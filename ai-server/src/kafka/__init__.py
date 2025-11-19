"""
Kafka Module
Handles Kafka consumer and producer for sensor data and alerts
"""

from .consumer import SensorDataConsumer, run_consumer
from .producer import AlertProducer, get_alert_producer

__all__ = [
    "SensorDataConsumer",
    "run_consumer",
    "AlertProducer",
    "get_alert_producer"
]