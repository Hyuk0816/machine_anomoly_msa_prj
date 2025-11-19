"""
Kafka Consumer 실행 스크립트

센서 데이터를 소비하고 실시간 이상 탐지를 수행합니다.
"""
import logging
from src.kafka.consumer import run_consumer
from src.config import settings

# 로깅 설정
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL),
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

if __name__ == "__main__":
    logger.info("=" * 50)
    logger.info("Kafka Consumer 시작...")
    logger.info(f"Topic: {settings.KAFKA_TOPIC_SENSOR}")
    logger.info(f"Group ID: {settings.KAFKA_GROUP_ID}")
    logger.info("=" * 50)

    try:
        run_consumer()
    except KeyboardInterrupt:
        logger.info("\nConsumer 중지됨 (KeyboardInterrupt)")
    except Exception as e:
        logger.error(f"Consumer 실행 중 오류: {e}")
        raise