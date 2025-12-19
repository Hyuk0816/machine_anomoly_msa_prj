"""
Cache Module
- Machine type caching (PostgreSQL + TTL)
- Machine ID caching (Redis)
"""

from .machine_cache import MachineTypeCache, get_machine_cache
from .redis_client import (
    MachineIdCache,
    get_machine_id_cache,
    get_machine_id,
    close_redis_cache,
)

__all__ = [
    "MachineTypeCache",
    "get_machine_cache",
    "MachineIdCache",
    "get_machine_id_cache",
    "get_machine_id",
    "close_redis_cache",
]