"""
데이터베이스 모델 정의

Outbox 패턴을 위한 SQLAlchemy 모델
"""
from datetime import datetime, timezone, timedelta
from typing import Optional
from sqlalchemy import String, DateTime, Boolean, Integer, Text, JSON
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column
import uuid


class Base(DeclarativeBase):
    """SQLAlchemy Declarative Base 클래스"""
    pass


class Outbox(Base):
    """
    Outbox 패턴 테이블

    이상 탐지 이벤트를 저장하여 Debezium CDC가
    이를 Kafka로 발행하도록 합니다.
    """
    __tablename__ = "outbox"

    # 기본키
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)

    # 이벤트 식별자 (UUID)
    aggregate_id: Mapped[str] = mapped_column(
        String(36),
        nullable=False,
        comment="이벤트 집합 ID (machine_id와 timestamp 조합)"
    )

    # 이벤트 타입
    event_type: Mapped[str] = mapped_column(
        String(100),
        nullable=False,
        comment="이벤트 타입 (예: anomaly_detected)"
    )

    # 페이로드 (JSON)
    payload: Mapped[dict] = mapped_column(
        JSON,
        nullable=False,
        comment="이벤트 데이터 (JSON 형식)"
    )

    # 생성 시각
    created_at: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=False,
        default=datetime.utcnow,
        comment="이벤트 생성 시각 (UTC)"
    )

    # 처리 상태
    processed: Mapped[bool] = mapped_column(
        Boolean,
        nullable=False,
        default=False,
        comment="CDC 처리 완료 여부"
    )

    # 처리 시각
    processed_at: Mapped[Optional[datetime]] = mapped_column(
        DateTime,
        nullable=True,
        comment="CDC 처리 완료 시각 (UTC)"
    )

    def __repr__(self) -> str:
        return (
            f"Outbox(id={self.id}, "
            f"aggregate_id='{self.aggregate_id}', "
            f"event_type='{self.event_type}', "
            f"created_at={self.created_at})"
        )

    @classmethod
    def create_anomaly_event(
        cls,
        machine_id: int,
        sensor_data: dict,
        prediction_result: dict
    ) -> "Outbox":
        """
        이상 탐지 이벤트 생성 헬퍼 메서드

        Args:
            machine_id: 머신 ID
            sensor_data: 원본 센서 데이터
            prediction_result: 예측 결과

        Returns:
            Outbox 인스턴스
        """
        # 한국 시간대 (UTC+9)
        kst = timezone(timedelta(hours=9))
        now_kst = datetime.now(kst)

        # 고유 aggregate_id 생성 (machine_id + timestamp)
        aggregate_id = f"{machine_id}_{int(now_kst.timestamp() * 1000)}"

        # 페이로드 구성
        # detected_at을 한국 시간(KST)으로 포맷 (Java LocalDateTime 호환)
        # timezone 정보를 제거한 naive datetime 문자열로 변환
        detected_at_str = now_kst.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]  # 마이크로초를 밀리초로

        payload = {
            "machine_id": machine_id,
            "sensor_data": sensor_data,
            "prediction": {
                "is_anomaly": prediction_result["is_anomaly"],
                "anomaly_probability": prediction_result.get("anomaly_probability"),
                "machine_type": prediction_result["machine_type"],
                "severity": prediction_result.get("severity", "UNKNOWN")
            },
            "detected_at": detected_at_str
        }

        return cls(
            aggregate_id=aggregate_id,
            event_type="anomaly_detected",
            payload=payload,
            created_at=now_kst.replace(tzinfo=None),  # DB 저장도 한국 시간, timezone 정보 제거
            processed=False
        )