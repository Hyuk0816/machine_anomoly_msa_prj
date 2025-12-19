"""
AnomalyHistory API 라우터

이상 탐지 이력 조회 API
"""
from fastapi import APIRouter, HTTPException, status, Query
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime
import logging

from ..db.repositories import get_anomaly_repository

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/anomaly-histories", tags=["Anomaly History"])


class SensorDataResponse(BaseModel):
    """센서 데이터 응답 모델"""
    airTemperature: float
    processTemperature: float
    rotationalSpeed: float
    torque: float
    toolWear: float


class AnomalyHistoryResponse(BaseModel):
    """AnomalyHistory 응답 모델"""
    id: int
    machine_id: int
    detected_at: datetime
    anomaly_probability: float
    sensor_data: SensorDataResponse
    severity: str
    created_at: datetime

    class Config:
        from_attributes = True


@router.get("/", response_model=List[AnomalyHistoryResponse])
async def get_recent_anomalies(
    limit: int = Query(default=100, ge=1, le=1000, description="Maximum number of results"),
    severity: Optional[str] = Query(default=None, description="Filter by severity (WARNING, ALERT, CRITICAL)")
):
    """
    최근 이상 탐지 이력 조회

    Args:
        limit: 최대 조회 개수 (1~1000)
        severity: 심각도 필터 (선택)

    Returns:
        AnomalyHistory 리스트
    """
    try:
        repository = get_anomaly_repository()
        anomalies = repository.get_recent_anomalies(limit=limit, severity=severity)

        return [
            AnomalyHistoryResponse(
                id=anomaly.id,
                machine_id=anomaly.machine_id,
                detected_at=anomaly.detected_at,
                anomaly_probability=anomaly.anomaly_probability,
                sensor_data=SensorDataResponse(**anomaly.sensor_data),
                severity=anomaly.severity,
                created_at=anomaly.created_at
            )
            for anomaly in anomalies
        ]

    except Exception as e:
        logger.error(f"Failed to get recent anomalies: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to get anomaly histories: {str(e)}"
        )


@router.get("/search", response_model=List[AnomalyHistoryResponse])
async def search_anomalies(
    start_date: datetime = Query(..., description="Start date (ISO format)"),
    end_date: datetime = Query(..., description="End date (ISO format)"),
    machine_id: Optional[int] = Query(default=None, description="Filter by machine ID"),
    severity: Optional[str] = Query(default=None, description="Filter by severity"),
    limit: int = Query(default=1000, ge=1, le=10000, description="Maximum number of results")
):
    """
    기간별 이상 탐지 이력 검색

    Args:
        start_date: 시작 일시 (필수)
        end_date: 종료 일시 (필수)
        machine_id: 머신 ID 필터 (선택)
        severity: 심각도 필터 (선택)
        limit: 최대 조회 개수 (1~10000)

    Returns:
        AnomalyHistory 리스트
    """
    try:
        repository = get_anomaly_repository()
        anomalies = repository.get_anomalies_by_period(
            start_date=start_date,
            end_date=end_date,
            machine_id=machine_id,
            severity=severity,
            limit=limit
        )

        return [
            AnomalyHistoryResponse(
                id=anomaly.id,
                machine_id=anomaly.machine_id,
                detected_at=anomaly.detected_at,
                anomaly_probability=anomaly.anomaly_probability,
                sensor_data=SensorDataResponse(**anomaly.sensor_data),
                severity=anomaly.severity,
                created_at=anomaly.created_at
            )
            for anomaly in anomalies
        ]

    except Exception as e:
        logger.error(f"Failed to search anomalies: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to search anomaly histories: {str(e)}"
        )


@router.get("/machine/{machine_id}", response_model=List[AnomalyHistoryResponse])
async def get_anomalies_by_machine(
    machine_id: int,
    limit: int = Query(default=100, ge=1, le=1000, description="Maximum number of results")
):
    """
    특정 머신의 이상 탐지 이력 조회

    Args:
        machine_id: 머신 ID (path parameter)
        limit: 최대 조회 개수 (1~1000)

    Returns:
        AnomalyHistory 리스트
    """
    try:
        repository = get_anomaly_repository()
        anomalies = repository.get_anomalies_by_machine(
            machine_id=machine_id,
            limit=limit
        )

        if not anomalies:
            return []

        return [
            AnomalyHistoryResponse(
                id=anomaly.id,
                machine_id=anomaly.machine_id,
                detected_at=anomaly.detected_at,
                anomaly_probability=anomaly.anomaly_probability,
                sensor_data=SensorDataResponse(**anomaly.sensor_data),
                severity=anomaly.severity,
                created_at=anomaly.created_at
            )
            for anomaly in anomalies
        ]

    except Exception as e:
        logger.error(f"Failed to get anomalies for machine {machine_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to get anomaly histories for machine: {str(e)}"
        )