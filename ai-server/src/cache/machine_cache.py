"""
머신 타입 캐시 모듈

PostgreSQL에서 머신 ID → 타입 매핑을 조회하고 TTL 캐시에 저장합니다.
"""
from cachetools import TTLCache
from sqlalchemy import create_engine, select, text
from sqlalchemy.orm import Session
from typing import Optional
import logging

from ..config import settings

logger = logging.getLogger(__name__)


class MachineTypeCache:
    """
    머신 타입 캐시 관리자

    Portal DB에서 머신 ID → 타입(L/M/H) 매핑을 조회하고
    TTLCache에 저장하여 반복 쿼리를 최소화합니다.
    """

    def __init__(self):
        """캐시 초기화 및 DB 엔진 생성"""
        # TTL 캐시 생성 (환경 설정에서 TTL 및 최대 크기 가져오기)
        self.cache = TTLCache(
            maxsize=settings.CACHE_MAXSIZE,
            ttl=settings.CACHE_TTL
        )

        # Portal DB 엔진 생성 (머신 정보 조회용)
        self.engine = create_engine(
            settings.portal_db_url,
            pool_pre_ping=True,  # 연결 검증
            pool_size=5,
            max_overflow=10
        )

        logger.info(
            f"MachineTypeCache 초기화 완료 "
            f"(TTL: {settings.CACHE_TTL}초, 최대크기: {settings.CACHE_MAXSIZE})"
        )

    def get_machine_type(self, machine_id: int) -> Optional[str]:
        """
        머신 ID로 머신 타입 조회 (캐시 우선, 미스 시 DB 조회)

        Args:
            machine_id: 조회할 머신 ID

        Returns:
            머신 타입 문자열 ('L', 'M', 'H') 또는 None (존재하지 않는 경우)

        Raises:
            RuntimeError: DB 조회 중 오류 발생 시
        """
        # 캐시에서 먼저 조회
        if machine_id in self.cache:
            logger.debug(f"캐시 히트: 머신 ID {machine_id} → {self.cache[machine_id]}")
            return self.cache[machine_id]

        # 캐시 미스 - DB에서 조회
        logger.debug(f"캐시 미스: 머신 ID {machine_id}, DB 조회 중...")
        machine_type = self._fetch_from_db(machine_id)

        # DB에서 찾았으면 캐시에 저장
        if machine_type is not None:
            self.cache[machine_id] = machine_type
            logger.debug(f"캐시 저장: 머신 ID {machine_id} → {machine_type}")

        return machine_type

    def _fetch_from_db(self, machine_id: int) -> Optional[str]:
        """
        DB에서 머신 타입 조회

        Args:
            machine_id: 조회할 머신 ID

        Returns:
            머신 타입 문자열 또는 None

        Raises:
            RuntimeError: DB 조회 중 오류 발생 시
        """
        try:
            with Session(self.engine) as session:
                # Machine 테이블에서 type 컬럼 조회
                # SQLAlchemy 2.0 스타일 쿼리 사용
                query = text(
                    "SELECT type FROM machine WHERE machine_id = :machine_id"
                )
                result = session.execute(
                    query,
                    {"machine_id": machine_id}
                ).first()

                if result is not None:
                    machine_type = result[0]
                    logger.info(f"DB 조회 성공: 머신 ID {machine_id} → {machine_type}")
                    return machine_type
                else:
                    logger.warning(f"머신 ID {machine_id}를 DB에서 찾을 수 없음")
                    return None

        except Exception as e:
            logger.error(f"DB 조회 중 오류 발생 (머신 ID: {machine_id}): {e}")
            raise RuntimeError(f"머신 타입 조회 실패: {e}")

    def invalidate(self, machine_id: int) -> None:
        """
        특정 머신 ID의 캐시 무효화

        Args:
            machine_id: 무효화할 머신 ID
        """
        if machine_id in self.cache:
            del self.cache[machine_id]
            logger.info(f"캐시 무효화: 머신 ID {machine_id}")

    def clear_cache(self) -> None:
        """전체 캐시 클리어"""
        self.cache.clear()
        logger.info("전체 캐시 클리어 완료")

    def get_cache_info(self) -> dict:
        """
        캐시 상태 정보 조회

        Returns:
            캐시 통계 딕셔너리
        """
        return {
            'current_size': len(self.cache),
            'max_size': self.cache.maxsize,
            'ttl_seconds': self.cache.ttl,
            'cached_machine_ids': list(self.cache.keys())
        }

    def warmup(self, machine_ids: list) -> dict:
        """
        여러 머신 ID에 대해 캐시 사전 로딩 (워밍업)

        Args:
            machine_ids: 사전 로드할 머신 ID 리스트

        Returns:
            워밍업 결과 딕셔너리 (성공/실패 개수)
        """
        success_count = 0
        failure_count = 0

        for machine_id in machine_ids:
            try:
                machine_type = self.get_machine_type(machine_id)
                if machine_type is not None:
                    success_count += 1
                else:
                    failure_count += 1
            except Exception as e:
                logger.error(f"워밍업 실패 (머신 ID: {machine_id}): {e}")
                failure_count += 1

        logger.info(
            f"캐시 워밍업 완료: 성공 {success_count}, 실패 {failure_count}"
        )

        return {
            'success': success_count,
            'failure': failure_count,
            'total': len(machine_ids)
        }

    def close(self) -> None:
        """DB 엔진 종료"""
        self.engine.dispose()
        logger.info("MachineTypeCache DB 연결 종료")


# 모듈 수준 싱글톤 인스턴스 (지연 로딩)
_cache_instance: Optional[MachineTypeCache] = None


def get_machine_cache() -> MachineTypeCache:
    """
    싱글톤 캐시 인스턴스 반환

    Returns:
        초기화된 MachineTypeCache 인스턴스
    """
    global _cache_instance
    if _cache_instance is None:
        _cache_instance = MachineTypeCache()
    return _cache_instance
