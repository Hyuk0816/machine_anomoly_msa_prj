"""
Redis Client Module for Machine Name to ID Mapping

Portal에서 설비 CUD 시 저장한 machine:name:{name} -> {machineId} 매핑을 조회합니다.
LLM Tool에서 설비 이름으로 machineId를 조회할 때 사용합니다.
"""
import logging
from typing import Optional

from redis import ConnectionPool, Redis
from redis.exceptions import RedisError

from ..config import settings

logger = logging.getLogger(__name__)

MACHINE_NAME_KEY_PREFIX = "machine:name:"


class MachineIdCache:
    """
    Redis 기반 머신 이름 -> ID 매핑 캐시
    """

    def __init__(self):
        self._pool = ConnectionPool.from_url(
            settings.redis_url,
            max_connections=settings.REDIS_MAX_CONNECTIONS,
            socket_timeout=settings.REDIS_SOCKET_TIMEOUT,
            decode_responses=settings.REDIS_DECODE_RESPONSES,
        )
        self._client = Redis.from_pool(self._pool)
        logger.info(f"Redis 연결 초기화 완료 ({settings.REDIS_HOST}:{settings.REDIS_PORT})")

    def get_machine_id(self, machine_name: str) -> Optional[int]:
        """
        머신 이름으로 머신 ID 조회

        Args:
            machine_name: 조회할 머신 이름

        Returns:
            머신 ID 또는 None
        """
        if not machine_name:
            return None

        key = f"{MACHINE_NAME_KEY_PREFIX}{machine_name}"

        try:
            value = self._client.get(key)
            if value is not None:
                return int(value)
            return None
        except (RedisError, ValueError) as e:
            logger.error(f"Redis 조회 실패 (machine_name: {machine_name}): {e}")
            raise

    def close(self) -> None:
        """Redis 연결 종료"""
        self._client.close()
        self._pool.disconnect()
        logger.info("Redis 연결 종료 완료")


_instance: Optional[MachineIdCache] = None


def get_machine_id_cache() -> MachineIdCache:
    """싱글톤 인스턴스 반환"""
    global _instance
    if _instance is None:
        _instance = MachineIdCache()
    return _instance


def get_machine_id(machine_name: str) -> Optional[int]:
    """머신 이름으로 ID 조회 (헬퍼 함수)"""
    return get_machine_id_cache().get_machine_id(machine_name)


def close_redis_cache() -> None:
    """Redis 연결 종료"""
    global _instance
    if _instance is not None:
        _instance.close()
        _instance = None