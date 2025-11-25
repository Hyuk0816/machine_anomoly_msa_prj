"""
ML 모델 로더 모듈

학습된 XGBoost 모델과 관련 아티팩트를 로드하고 관리합니다.
싱글톤 패턴을 사용하여 모델을 한 번만 로드합니다.
"""
import joblib
from pathlib import Path
from typing import Any, Optional, Dict, List, Tuple
import logging
import numpy as np

from ..config import settings

logger = logging.getLogger(__name__)

# SHAP import with lazy loading (optional dependency)
try:
    import shap
    SHAP_AVAILABLE = True
except ImportError:
    SHAP_AVAILABLE = False
    logger.warning("SHAP not installed. Feature explanation will be limited.")


class ModelLoader:
    """
    ML 모델 및 아티팩트 로더 (싱글톤)

    학습된 XGBoost 모델, 스케일러, 인코더, 특징명을 로드하고
    애플리케이션 생명주기 동안 메모리에 유지합니다.
    """

    def __init__(self):
        """모델 로더 초기화"""
        self.model = None
        self.model_info = {}
        self.shap_explainer = None
        self._load_model()
        self._initialize_shap()

    def _load_model(self) -> None:
        """학습된 XGBoost 모델 로드"""
        try:
            model_path = settings.get_model_path(settings.MODEL_PATH)

            if not model_path.exists():
                raise FileNotFoundError(
                    f"모델 파일을 찾을 수 없음: {model_path}\n"
                    f"ai-model 디렉토리에서 모델을 먼저 학습해주세요."
                )

            # joblib로 XGBoost 모델 로드
            self.model = joblib.load(model_path)

            # 모델 정보 저장
            self.model_info = {
                'model_path': str(model_path),
                'model_type': type(self.model).__name__,
                'loaded': True
            }

            # XGBoost 모델 특정 정보 추출
            if hasattr(self.model, 'n_features_in_'):
                self.model_info['n_features'] = self.model.n_features_in_

            if hasattr(self.model, 'n_classes_'):
                self.model_info['n_classes'] = self.model.n_classes_

            logger.info(f"모델 로드 완료: {model_path}")
            logger.info(f"모델 타입: {self.model_info['model_type']}")

        except FileNotFoundError as e:
            logger.error(f"모델 파일 없음: {e}")
            raise
        except Exception as e:
            logger.error(f"모델 로드 중 오류 발생: {e}")
            raise

    def predict(self, features: Any) -> int:
        """
        이진 분류 예측 수행 (정상/이상)

        Args:
            features: 전처리된 특징 배열 (스케일 완료)

        Returns:
            예측 결과 (0: 정상, 1: 이상)

        Raises:
            RuntimeError: 모델이 로드되지 않은 경우
        """
        if self.model is None:
            raise RuntimeError("모델이 로드되지 않았습니다.")

        try:
            prediction = self.model.predict(features)
            return int(prediction[0])
        except Exception as e:
            logger.error(f"예측 중 오류 발생: {e}")
            raise

    def predict_proba(self, features: Any) -> tuple:
        """
        클래스별 확률 예측

        Args:
            features: 전처리된 특징 배열 (스케일 완료)

        Returns:
            튜플 (정상 확률, 이상 확률)

        Raises:
            RuntimeError: 모델이 로드되지 않은 경우
        """
        if self.model is None:
            raise RuntimeError("모델이 로드되지 않았습니다.")

        try:
            probabilities = self.model.predict_proba(features)
            # probabilities는 [[prob_class_0, prob_class_1]] 형태
            normal_prob = float(probabilities[0][0])
            anomaly_prob = float(probabilities[0][1])
            return normal_prob, anomaly_prob
        except Exception as e:
            logger.error(f"확률 예측 중 오류 발생: {e}")
            raise

    def get_model_info(self) -> dict:
        """
        모델 정보 반환

        Returns:
            모델 메타데이터 딕셔너리
        """
        return self.model_info.copy()

    def is_loaded(self) -> bool:
        """
        모델 로드 상태 확인

        Returns:
            모델이 로드되었으면 True, 아니면 False
        """
        return self.model is not None

    def _initialize_shap(self) -> None:
        """SHAP explainer 초기화"""
        if not SHAP_AVAILABLE:
            logger.info("SHAP 라이브러리 미설치 - Feature explanation 비활성화")
            return

        if self.model is None:
            logger.warning("모델이 로드되지 않아 SHAP explainer 초기화 실패")
            return

        try:
            # XGBoost 모델용 TreeExplainer 생성
            self.shap_explainer = shap.TreeExplainer(self.model)
            logger.info("SHAP explainer 초기화 완료")
        except Exception as e:
            logger.error(f"SHAP explainer 초기화 실패: {e}")
            self.shap_explainer = None

    def get_feature_importance(self) -> Optional[Dict[str, float]]:
        """
        모델의 전역 feature importance 반환

        Returns:
            특징명: 중요도 딕셔너리, 또는 None
        """
        if self.model is None or not hasattr(self.model, 'feature_importances_'):
            return None

        try:
            # XGBoost의 feature_importances_ 속성 사용
            importances = self.model.feature_importances_

            # feature_names 로드 (preprocessor에서 가져옴)
            from ..preprocessing.feature_engineering import get_preprocessor
            preprocessor = get_preprocessor()
            feature_names = preprocessor.feature_names

            if feature_names is None or len(feature_names) != len(importances):
                logger.warning("Feature names 불일치")
                return None

            # 딕셔너리로 변환 및 정렬
            importance_dict = dict(zip(feature_names, importances))
            sorted_importance = dict(
                sorted(importance_dict.items(), key=lambda x: x[1], reverse=True)
            )

            return sorted_importance

        except Exception as e:
            logger.error(f"Feature importance 계산 실패: {e}")
            return None

    def explain_prediction(
        self,
        features: Any,
        feature_names: List[str],
        top_k: int = 5
    ) -> Optional[Dict[str, Any]]:
        """
        SHAP 값을 사용하여 개별 예측 설명

        Args:
            features: 전처리된 특징 배열
            feature_names: 특징 이름 리스트
            top_k: 상위 K개 중요 특징만 반환

        Returns:
            설명 딕셔너리 또는 None
        """
        if self.shap_explainer is None:
            return None

        try:
            # SHAP 값 계산
            shap_values = self.shap_explainer.shap_values(features)

            # 2D array인 경우 첫 번째 샘플만 (배치가 아닌 경우)
            if isinstance(shap_values, list):
                # Binary classification: [class_0_shap, class_1_shap]
                shap_values = shap_values[1]  # 이상 클래스(1)의 SHAP 값 사용

            # 1D로 변환
            if len(shap_values.shape) > 1:
                shap_values = shap_values[0]

            # 절대값 기준으로 정렬하여 상위 K개 추출
            abs_shap = np.abs(shap_values)
            top_indices = np.argsort(abs_shap)[::-1][:top_k]

            # 결과 구성
            explanations = []
            for idx in top_indices:
                explanations.append({
                    'feature': feature_names[idx],
                    'shap_value': float(shap_values[idx]),
                    'contribution': 'increases anomaly' if shap_values[idx] > 0 else 'decreases anomaly',
                    'magnitude': float(abs_shap[idx])
                })

            return {
                'top_features': explanations,
                'base_value': float(self.shap_explainer.expected_value) if hasattr(self.shap_explainer, 'expected_value') else None
            }

        except Exception as e:
            logger.error(f"SHAP 설명 생성 실패: {e}")
            return None


# 모듈 수준 싱글톤 인스턴스 (지연 로딩)
_model_loader_instance: Optional[ModelLoader] = None


def get_model_loader() -> ModelLoader:
    """
    싱글톤 모델 로더 인스턴스 반환

    Returns:
        초기화된 ModelLoader 인스턴스
    """
    global _model_loader_instance
    if _model_loader_instance is None:
        _model_loader_instance = ModelLoader()
    return _model_loader_instance


def reload_model() -> ModelLoader:
    """
    모델 강제 재로드 (개발/테스트용)

    Returns:
        새로 로드된 ModelLoader 인스턴스
    """
    global _model_loader_instance
    _model_loader_instance = ModelLoader()
    logger.info("모델이 강제로 재로드되었습니다.")
    return _model_loader_instance