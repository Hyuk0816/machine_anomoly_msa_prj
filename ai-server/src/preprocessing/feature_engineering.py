"""
실시간 센서 데이터 전처리를 위한 특징 공학 모듈

Kafka로 들어오는 JSON 센서 데이터를 모델 입력 가능한 NumPy 배열로 변환합니다.
학습 시 사용된 특징 공학 파이프라인과 정확히 동일하게 처리해야 합니다.
"""
import numpy as np
import pandas as pd
import joblib
from pathlib import Path
from typing import Dict, Any, Tuple
import logging

from ..config import settings

logger = logging.getLogger(__name__)


class SensorDataPreprocessor:
    """
    실시간 이상 탐지를 위한 센서 데이터 전처리기

    핵심 요구사항:
    - 특징 공학은 학습 파이프라인과 정확히 일치해야 함
    - 특징 순서는 feature_names.pkl과 일치해야 함
    - 스케일러는 절대 재학습하지 않음 - 항상 저장된 scaler.pkl 사용
    - NaN/Inf 값에 대한 입력 검증 필수
    """

    def __init__(self):
        """저장된 아티팩트로 전처리기 초기화"""
        self.scaler = None
        self.label_encoder = None
        self.feature_names = None
        self._load_artifacts()

    def _load_artifacts(self) -> None:
        """저장된 전처리 아티팩트(스케일러, 인코더, 특징명) 로드"""
        try:
            # 스케일러 로드
            scaler_path = settings.get_model_path(settings.SCALER_PATH)
            self.scaler = joblib.load(scaler_path)
            logger.info(f"스케일러 로드 완료: {scaler_path}")

            # 머신 타입 레이블 인코더 로드
            encoder_path = settings.get_model_path(settings.ENCODER_PATH)
            self.label_encoder = joblib.load(encoder_path)
            logger.info(f"레이블 인코더 로드 완료: {encoder_path}")

            # 특징명 로드 (올바른 순서 보장)
            feature_names_path = settings.get_model_path(settings.FEATURE_NAMES_PATH)
            self.feature_names = joblib.load(feature_names_path)
            logger.info(f"특징명 로드 완료: {self.feature_names}")

        except FileNotFoundError as e:
            logger.error(f"필수 전처리 아티팩트를 찾을 수 없음: {e}")
            raise
        except Exception as e:
            logger.error(f"전처리 아티팩트 로드 중 오류: {e}")
            raise

    def preprocess(
        self,
        sensor_data: Dict[str, Any],
        machine_type: str
    ) -> Tuple[np.ndarray, Dict[str, float]]:
        """
        센서 JSON 데이터를 모델 입력 가능한 NumPy 배열로 변환

        Args:
            sensor_data: 센서 측정값을 담은 딕셔너리
                필수 키: airTemperature, processTemperature,
                        rotationalSpeed, torque, toolWear
            machine_type: 머신 타입 문자열 ('L', 'M', 또는 'H')

        Returns:
            튜플:
                - 모델 입력 가능한 스케일된 특징 배열 (1, n_features)
                - 공학적 특징 딕셔너리 (디버깅/로깅용)

        Raises:
            ValueError: 입력 검증 실패 시
        """
        # 1단계: JSON에서 기본 특징 추출
        base_features = self._extract_base_features(sensor_data)

        # 2단계: 머신 타입 인코딩
        type_encoded = self._encode_machine_type(machine_type)

        # 3단계: 파생 특징 생성 (학습 시와 반드시 일치)
        engineered_features = self._engineer_features(base_features)

        # 4단계: 올바른 순서로 모든 특징 결합
        feature_dict = {
            'Type_encoded': type_encoded,
            'Air_temperature_K': base_features['airTemperature'],
            'Process_temperature_K': base_features['processTemperature'],
            'Rotational_speed_rpm': base_features['rotationalSpeed'],
            'Torque_Nm': base_features['torque'],
            'Tool_wear_min': base_features['toolWear'],
            'Temp_diff': engineered_features['Temp_diff'],
            'Power': engineered_features['Power'],
            'Tool_wear_rate': engineered_features['Tool_wear_rate'],
            'Torque_speed_ratio': engineered_features['Torque_speed_ratio'],
            'Temp_toolwear': engineered_features['Temp_toolwear']
        }

        # 5단계: 학습 시 순서와 정확히 동일하게 특징 배열 생성
        feature_array = self._create_feature_array(feature_dict)

        # 6단계: NaN/Inf 검증
        self._validate_features(feature_array)

        # 7단계: DataFrame으로 변환 (feature names 포함)
        feature_df = pd.DataFrame(feature_array, columns=self.feature_names)

        # 8단계: 저장된 스케일러로 스케일링 (절대 재학습 금지)
        scaled_features = self.scaler.transform(feature_df)

        logger.debug(f"전처리된 특징: {feature_dict}")

        return scaled_features, feature_dict

    def _extract_base_features(self, sensor_data: Dict[str, Any]) -> Dict[str, float]:
        """
        JSON에서 기본 센서 특징 추출 및 검증

        Args:
            sensor_data: 원시 센서 데이터 딕셔너리

        Returns:
            검증된 기본 특징 딕셔너리

        Raises:
            ValueError: 필수 필드가 누락되거나 유효하지 않은 경우
        """
        required_fields = [
            'airTemperature',
            'processTemperature',
            'rotationalSpeed',
            'torque',
            'toolWear'
        ]

        # 필수 필드 존재 확인
        missing_fields = [f for f in required_fields if f not in sensor_data]
        if missing_fields:
            raise ValueError(f"필수 필드 누락: {missing_fields}")

        # float으로 추출 및 변환
        try:
            base_features = {
                'airTemperature': float(sensor_data['airTemperature']),
                'processTemperature': float(sensor_data['processTemperature']),
                'rotationalSpeed': float(sensor_data['rotationalSpeed']),
                'torque': float(sensor_data['torque']),
                'toolWear': float(sensor_data['toolWear'])
            }
        except (ValueError, TypeError) as e:
            raise ValueError(f"센서 데이터 타입이 유효하지 않음: {e}")

        return base_features

    def _encode_machine_type(self, machine_type: str) -> int:
        """
        저장된 레이블 인코더로 머신 타입 인코딩

        Args:
            machine_type: 머신 타입 문자열 ('L', 'M', 또는 'H')

        Returns:
            정수로 인코딩된 타입

        Raises:
            ValueError: 머신 타입이 유효하지 않은 경우
        """
        if machine_type not in self.label_encoder.classes_:
            valid_types = list(self.label_encoder.classes_)
            raise ValueError(
                f"유효하지 않은 머신 타입 '{machine_type}'. "
                f"유효한 타입: {valid_types}"
            )

        # transform 메서드 사용 (배열 반환하므로 첫 번째 요소 추출)
        encoded = self.label_encoder.transform([machine_type])[0]
        return int(encoded)

    def _engineer_features(self, base_features: Dict[str, float]) -> Dict[str, float]:
        """
        학습 시와 정확히 동일한 로직으로 파생 특징 생성

        특징 공학 (학습과 반드시 일치):
        1. Temp_diff = Process_temperature - Air_temperature
        2. Power = Torque * Rotational_speed / 1000
        3. Tool_wear_rate = Tool_wear / (Rotational_speed + 1)
        4. Torque_speed_ratio = Torque / (Rotational_speed + 1)
        5. Temp_toolwear = Process_temperature * Tool_wear

        Args:
            base_features: 기본 센서 특징 딕셔너리

        Returns:
            공학적 특징 딕셔너리
        """
        air_temp = base_features['airTemperature']
        process_temp = base_features['processTemperature']
        rpm = base_features['rotationalSpeed']
        torque = base_features['torque']
        tool_wear = base_features['toolWear']

        engineered = {
            'Temp_diff': process_temp - air_temp,
            'Power': torque * rpm / 1000.0,
            'Tool_wear_rate': tool_wear / (rpm + 1.0),
            'Torque_speed_ratio': torque / (rpm + 1.0),
            'Temp_toolwear': process_temp * tool_wear
        }

        return engineered

    def _create_feature_array(self, feature_dict: Dict[str, float]) -> np.ndarray:
        """
        학습 시와 정확히 동일한 순서로 NumPy 배열 생성

        Args:
            feature_dict: 모든 특징(기본 + 공학적) 딕셔너리

        Returns:
            학습 순서와 일치하는 (1, n_features) 형태의 NumPy 배열

        Raises:
            ValueError: 특징명이 학습과 일치하지 않는 경우
        """
        # 예상되는 모든 특징이 있는지 확인
        if len(feature_dict) != len(self.feature_names):
            raise ValueError(
                f"특징 개수 불일치. 예상: {len(self.feature_names)}, "
                f"실제: {len(feature_dict)}"
            )

        # feature_names의 정확한 순서대로 배열 생성
        try:
            feature_values = [feature_dict[name] for name in self.feature_names]
            feature_array = np.array([feature_values])  # 형태: (1, n_features)
        except KeyError as e:
            raise ValueError(f"예상된 특징이 누락됨: {e}")

        return feature_array

    def _validate_features(self, feature_array: np.ndarray) -> None:
        """
        특징 배열에서 NaN 및 Inf 값 검증

        Args:
            feature_array: 검증할 NumPy 배열

        Raises:
            ValueError: NaN 또는 Inf 값이 감지된 경우
        """
        if np.isnan(feature_array).any():
            nan_indices = np.where(np.isnan(feature_array))[1]
            nan_features = [self.feature_names[i] for i in nan_indices]
            raise ValueError(f"NaN 값이 감지된 특징: {nan_features}")

        if np.isinf(feature_array).any():
            inf_indices = np.where(np.isinf(feature_array))[1]
            inf_features = [self.feature_names[i] for i in inf_indices]
            raise ValueError(f"무한대 값이 감지된 특징: {inf_features}")

    def get_feature_info(self) -> Dict[str, Any]:
        """
        로드된 전처리 아티팩트 정보 조회

        Returns:
            전처리 설정 정보 딕셔너리
        """
        return {
            'feature_count': len(self.feature_names),
            'feature_names': self.feature_names,
            'scaler_mean': self.scaler.mean_.tolist() if hasattr(self.scaler, 'mean_') else None,
            'scaler_scale': self.scaler.scale_.tolist() if hasattr(self.scaler, 'scale_') else None,
            'valid_machine_types': list(self.label_encoder.classes_),
            'scaler_path': str(settings.get_model_path(settings.SCALER_PATH)),
            'encoder_path': str(settings.get_model_path(settings.ENCODER_PATH))
        }


# 모듈 수준 싱글톤 인스턴스 (지연 로딩)
_preprocessor_instance = None


def get_preprocessor() -> SensorDataPreprocessor:
    """
    싱글톤 전처리기 인스턴스 반환

    Returns:
        초기화된 SensorDataPreprocessor 인스턴스
    """
    global _preprocessor_instance
    if _preprocessor_instance is None:
        _preprocessor_instance = SensorDataPreprocessor()
    return _preprocessor_instance