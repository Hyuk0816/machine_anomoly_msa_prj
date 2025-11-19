"""
ML 모델 로더 모듈

학습된 XGBoost 모델과 관련 아티팩트를 로드하고 관리합니다.
싱글톤 패턴을 사용하여 모델을 한 번만 로드합니다.
"""
import joblib
from pathlib import Path
from typing import Any, Optional
import logging

from ..config import settings

logger = logging.getLogger(__name__)


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
        self._load_model()

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