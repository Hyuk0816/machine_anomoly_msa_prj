"""
데이터베이스 레포지토리

Outbox 이벤트 저장 및 조회를 위한 레포지토리
"""
from typing import Optional, List
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session, sessionmaker
import logging

from .models import Base, Outbox
from ..config import settings

logger = logging.getLogger(__name__)


class OutboxRepository:
    """
    Outbox 이벤트 저장소

    트랜잭션 관리 및 Outbox 이벤트 영속화를 담당합니다.
    """

    def __init__(self):
        """AI Server DB 엔진 및 세션 팩토리 초기화"""
        # AI Server DB 엔진 생성
        self.engine = create_engine(
            settings.ai_db_url,
            pool_pre_ping=True,
            pool_size=5,
            max_overflow=10,
            echo=False  # SQL 로깅 비활성화 (운영 환경)
        )

        # 세션 팩토리 생성
        self.SessionLocal = sessionmaker(
            bind=self.engine,
            autocommit=False,
            autoflush=False
        )

        logger.info(f"OutboxRepository 초기화 완료: {settings.ai_db_url}")

    def create_tables(self) -> None:
        """
        데이터베이스 테이블 생성 (개발용)

        운영 환경에서는 Alembic 마이그레이션을 사용해야 합니다.
        """
        Base.metadata.create_all(bind=self.engine)
        logger.info("Outbox 테이블 생성 완료")

    def save_event(self, outbox_event: Outbox) -> Outbox:
        """
        Outbox 이벤트 저장

        Args:
            outbox_event: 저장할 Outbox 인스턴스

        Returns:
            저장된 Outbox 인스턴스 (ID 포함)

        Raises:
            RuntimeError: DB 저장 실패 시
        """
        try:
            with self.SessionLocal() as session:
                session.add(outbox_event)
                session.commit()
                session.refresh(outbox_event)

                logger.info(
                    f"Outbox 이벤트 저장 완료: "
                    f"ID={outbox_event.id}, "
                    f"aggregate_id={outbox_event.aggregate_id}"
                )

                return outbox_event

        except Exception as e:
            logger.error(f"Outbox 이벤트 저장 실패: {e}")
            raise RuntimeError(f"Outbox 저장 실패: {e}")

    def mark_as_processed(self, outbox_id: int) -> bool:
        """
        Outbox 이벤트를 처리 완료로 표시

        Args:
            outbox_id: Outbox ID

        Returns:
            성공 여부

        Raises:
            RuntimeError: DB 업데이트 실패 시
        """
        try:
            with self.SessionLocal() as session:
                # SQLAlchemy 2.0 스타일 쿼리
                stmt = select(Outbox).where(Outbox.id == outbox_id)
                outbox = session.scalar(stmt)

                if outbox is None:
                    logger.warning(f"Outbox ID {outbox_id}를 찾을 수 없음")
                    return False

                # 처리 완료로 표시
                from datetime import datetime
                outbox.processed = True
                outbox.processed_at = datetime.utcnow()

                session.commit()

                logger.info(f"Outbox 이벤트 처리 완료 표시: ID={outbox_id}")
                return True

        except Exception as e:
            logger.error(f"Outbox 처리 완료 표시 실패: {e}")
            raise RuntimeError(f"Outbox 업데이트 실패: {e}")

    def get_unprocessed_events(self, limit: int = 100) -> List[Outbox]:
        """
        미처리 Outbox 이벤트 조회

        Args:
            limit: 최대 조회 개수

        Returns:
            미처리 Outbox 이벤트 리스트
        """
        try:
            with self.SessionLocal() as session:
                # SQLAlchemy 2.0 스타일 쿼리
                stmt = (
                    select(Outbox)
                    .where(Outbox.processed == False)
                    .order_by(Outbox.created_at)
                    .limit(limit)
                )

                events = session.scalars(stmt).all()

                logger.debug(f"미처리 이벤트 {len(events)}개 조회 완료")
                return list(events)

        except Exception as e:
            logger.error(f"미처리 이벤트 조회 실패: {e}")
            return []

    def get_event_by_id(self, outbox_id: int) -> Optional[Outbox]:
        """
        ID로 Outbox 이벤트 조회

        Args:
            outbox_id: Outbox ID

        Returns:
            Outbox 인스턴스 또는 None
        """
        try:
            with self.SessionLocal() as session:
                stmt = select(Outbox).where(Outbox.id == outbox_id)
                event = session.scalar(stmt)
                return event

        except Exception as e:
            logger.error(f"Outbox 이벤트 조회 실패: {e}")
            return None

    def close(self) -> None:
        """DB 엔진 종료"""
        self.engine.dispose()
        logger.info("OutboxRepository DB 연결 종료")


# 모듈 수준 싱글톤 인스턴스 (지연 로딩)
_repository_instance: Optional[OutboxRepository] = None


def get_outbox_repository() -> OutboxRepository:
    """
    싱글톤 레포지토리 인스턴스 반환

    Returns:
        초기화된 OutboxRepository 인스턴스
    """
    global _repository_instance
    if _repository_instance is None:
        _repository_instance = OutboxRepository()
    return _repository_instance