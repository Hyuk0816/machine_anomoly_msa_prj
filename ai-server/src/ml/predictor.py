"""
예측기 모듈

전처리와 모델 추론을 통합하여 end-to-end 예측을 수행합니다.
"""
from typing import Dict, Any, Tuple
import logging

from .model_loader import get_model_loader
from ..preprocessing.feature_engineering import get_preprocessor

logger = logging.getLogger(__name__)


class AnomalyPredictor:
    """
    이상 탐지 예측기

    센서 데이터를 받아서 전처리 → 모델 추론 → 결과 반환까지
    전체 예측 파이프라인을 관리합니다.
    """

    def __init__(self):
        """예측기 초기화 (전처리기 및 모델 로더 싱글톤 사용)"""
        self.preprocessor = get_preprocessor()
        self.model_loader = get_model_loader()
        logger.info("AnomalyPredictor 초기화 완료")

    def predict(
        self,
        sensor_data: Dict[str, Any],
        machine_type: str,
        return_probabilities: bool = False
    ) -> Dict[str, Any]:
        """
        센서 데이터에 대한 이상 탐지 예측 수행

        Args:
            sensor_data: 센서 측정값 딕셔너리
                필수 키: airTemperature, processTemperature,
                        rotationalSpeed, torque, toolWear
            machine_type: 머신 타입 ('L', 'M', 'H')
            return_probabilities: True면 확률값도 반환

        Returns:
            예측 결과 딕셔너리:
                - is_anomaly (bool): 이상 여부
                - prediction (int): 0(정상) 또는 1(이상)
                - machine_type (str): 입력된 머신 타입
                - features (dict): 공학적 특징 (디버깅용)
                - normal_probability (float): 정상 확률 (옵션)
                - anomaly_probability (float): 이상 확률 (옵션)

        Raises:
            ValueError: 입력 데이터가 유효하지 않은 경우
            RuntimeError: 모델 추론 실패 시
        """
        try:
            # 1단계: 전처리 (특징 공학 + 스케일링)
            scaled_features, feature_dict = self.preprocessor.preprocess(
                sensor_data,
                machine_type
            )

            # 2단계: 모델 예측
            prediction = self.model_loader.predict(scaled_features)

            # 기본 결과 구성
            result = {
                'is_anomaly': bool(prediction == 1),
                'prediction': int(prediction),
                'machine_type': machine_type,
                'features': feature_dict
            }

            # 3단계: 확률값 계산 (옵션)
            if return_probabilities:
                normal_prob, anomaly_prob = self.model_loader.predict_proba(scaled_features)
                result['normal_probability'] = normal_prob
                result['anomaly_probability'] = anomaly_prob

                logger.debug(
                    f"예측 완료 - 이상: {result['is_anomaly']}, "
                    f"이상 확률: {anomaly_prob:.4f}"
                )
            else:
                logger.debug(f"예측 완료 - 이상: {result['is_anomaly']}")

            return result

        except ValueError as e:
            logger.error(f"입력 데이터 검증 실패: {e}")
            raise
        except Exception as e:
            logger.error(f"예측 중 오류 발생: {e}")
            raise RuntimeError(f"예측 실패: {e}")

    def predict_batch(
        self,
        sensor_data_list: list,
        machine_types: list,
        return_probabilities: bool = False
    ) -> list:
        """
        여러 센서 데이터에 대한 배치 예측

        Args:
            sensor_data_list: 센서 데이터 딕셔너리 리스트
            machine_types: 각 센서 데이터에 대응하는 머신 타입 리스트
            return_probabilities: True면 확률값도 반환

        Returns:
            예측 결과 딕셔너리 리스트

        Raises:
            ValueError: 입력 리스트 길이가 일치하지 않는 경우
        """
        if len(sensor_data_list) != len(machine_types):
            raise ValueError(
                f"센서 데이터 개수({len(sensor_data_list)})와 "
                f"머신 타입 개수({len(machine_types)})가 일치하지 않습니다."
            )

        results = []
        for sensor_data, machine_type in zip(sensor_data_list, machine_types):
            try:
                result = self.predict(sensor_data, machine_type, return_probabilities)
                results.append(result)
            except Exception as e:
                logger.error(f"배치 예측 중 오류 (머신 타입: {machine_type}): {e}")
                # 오류가 발생한 항목은 에러 정보와 함께 결과에 포함
                results.append({
                    'is_anomaly': None,
                    'prediction': None,
                    'machine_type': machine_type,
                    'error': str(e)
                })

        logger.info(f"배치 예측 완료: {len(results)}개 항목")
        return results

    def get_system_info(self) -> Dict[str, Any]:
        """
        예측 시스템 정보 조회

        Returns:
            전처리기 및 모델 정보를 포함한 딕셔너리
        """
        return {
            'preprocessor_info': self.preprocessor.get_feature_info(),
            'model_info': self.model_loader.get_model_info(),
            'status': 'ready' if self.model_loader.is_loaded() else 'not_ready'
        }


# 모듈 수준 싱글톤 인스턴스 (지연 로딩)
_predictor_instance = None


def get_predictor() -> AnomalyPredictor:
    """
    싱글톤 예측기 인스턴스 반환

    Returns:
        초기화된 AnomalyPredictor 인스턴스
    """
    global _predictor_instance
    if _predictor_instance is None:
        _predictor_instance = AnomalyPredictor()
    return _predictor_instance