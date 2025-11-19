"""
FastAPI 라우터

API 엔드포인트 정의
"""
from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field
from typing import Dict, Any, Optional
import logging

from ..ml.predictor import get_predictor
from ..cache.machine_cache import get_machine_cache
from ..db.repositories import get_outbox_repository
from ..kafka.producer import get_alert_producer
from ..config import settings

logger = logging.getLogger(__name__)

# API Router 생성
router = APIRouter()


# Request/Response 모델 정의
class PredictRequest(BaseModel):
    """예측 요청 모델"""
    machine_id: int = Field(..., description="머신 ID")
    sensor_data: Dict[str, float] = Field(
        ...,
        description="센서 데이터",
        example={
            "airTemperature": 300.5,
            "processTemperature": 310.2,
            "rotationalSpeed": 1500.0,
            "torque": 42.5,
            "toolWear": 100.0
        }
    )


class PredictResponse(BaseModel):
    """예측 응답 모델"""
    machine_id: int
    is_anomaly: bool
    prediction: int
    machine_type: str
    normal_probability: Optional[float] = None
    anomaly_probability: Optional[float] = None
    features: Optional[Dict[str, float]] = None


class HealthResponse(BaseModel):
    """헬스체크 응답 모델"""
    status: str
    version: str = "1.0.0"
    predictor_ready: bool
    cache_info: Dict[str, Any]
    model_info: Dict[str, Any]


class SystemInfoResponse(BaseModel):
    """시스템 정보 응답 모델"""
    config: Dict[str, Any]
    cache_info: Dict[str, Any]
    model_info: Dict[str, Any]
    kafka_config: Dict[str, str]


@router.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    """
    헬스체크 엔드포인트

    서비스 상태 및 준비 여부를 확인합니다.
    """
    try:
        predictor = get_predictor()
        cache = get_machine_cache()

        return HealthResponse(
            status="healthy",
            version="1.0.0",
            predictor_ready=True,
            cache_info=cache.get_cache_info(),
            model_info=predictor.get_system_info()["model_info"]
        )
    except Exception as e:
        logger.error(f"헬스체크 실패: {e}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Service unhealthy: {str(e)}"
        )


@router.get("/", tags=["Root"])
async def root():
    """루트 엔드포인트"""
    return {
        "service": "AI Anomaly Detection Server",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "health": "/health",
            "predict": "/predict",
            "system_info": "/system/info"
        }
    }


@router.post("/predict", response_model=PredictResponse, tags=["Prediction"])
async def predict_anomaly(request: PredictRequest):
    """
    이상 탐지 예측 엔드포인트 (수동 테스트용)

    실제 운영 환경에서는 Kafka Consumer가 자동으로 예측을 수행하지만,
    이 엔드포인트는 수동 테스트 및 디버깅 용도로 사용됩니다.
    """
    try:
        # 캐시에서 머신 타입 조회 (캐시 미스 시 자동 DB 조회)
        cache = get_machine_cache()
        machine_type = cache.get_machine_type(request.machine_id)

        if machine_type is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Machine ID {request.machine_id} not found in database"
            )

        # 예측 수행
        predictor = get_predictor()
        result = predictor.predict(
            sensor_data=request.sensor_data,
            machine_type=machine_type,
            return_probabilities=True
        )

        # 응답 생성
        return PredictResponse(
            machine_id=request.machine_id,
            is_anomaly=result["is_anomaly"],
            prediction=result["prediction"],
            machine_type=result["machine_type"],
            normal_probability=result.get("normal_probability"),
            anomaly_probability=result.get("anomaly_probability"),
            features=result.get("features")
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"예측 실패: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Prediction failed: {str(e)}"
        )


@router.get("/system/info", response_model=SystemInfoResponse, tags=["System"])
async def get_system_info():
    """
    시스템 정보 조회 엔드포인트

    AI Server의 설정 및 상태 정보를 조회합니다.
    """
    try:
        predictor = get_predictor()
        cache = get_machine_cache()

        return SystemInfoResponse(
            config={
                "api_host": settings.API_HOST,
                "api_port": settings.API_PORT,
                "log_level": settings.LOG_LEVEL,
                "cache_ttl": settings.CACHE_TTL,
                "cache_maxsize": settings.CACHE_MAXSIZE
            },
            cache_info=cache.get_cache_info(),
            model_info=predictor.get_system_info()["model_info"],
            kafka_config={
                "bootstrap_servers": settings.KAFKA_BOOTSTRAP_SERVERS,
                "sensor_topic": settings.KAFKA_TOPIC_SENSOR,
                "alert_topic": settings.KAFKA_TOPIC_ALERT,
                "group_id": settings.KAFKA_GROUP_ID
            }
        )

    except Exception as e:
        logger.error(f"시스템 정보 조회 실패: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to retrieve system info: {str(e)}"
        )


@router.get("/cache/stats", tags=["Cache"])
async def get_cache_stats():
    """
    캐시 통계 조회 엔드포인트

    머신 타입 캐시의 현재 상태를 조회합니다.
    """
    try:
        cache = get_machine_cache()
        return cache.get_cache_info()

    except Exception as e:
        logger.error(f"캐시 통계 조회 실패: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to retrieve cache stats: {str(e)}"
        )