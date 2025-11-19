"""
Kafka Producer 모듈

이상 탐지 알림을 Kafka anomaly-alerts 토픽으로 발행합니다.
"""
from kafka import KafkaProducer
from kafka.errors import KafkaError
import json
import logging
from typing import Optional, Dict, Any

from ..config import settings

logger = logging.getLogger(__name__)


class AlertProducer:
    """
    이상 탐지 알림 Producer

    anomaly-alerts 토픽으로 이상 탐지 이벤트를 발행합니다.
    """

    def __init__(self):
        """Kafka Producer 초기화"""
        try:
            self.producer = KafkaProducer(
                bootstrap_servers=settings.kafka_servers_list,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                key_serializer=lambda k: k.encode('utf-8') if k else None,
                acks='all',  # 모든 in-sync replica의 확인 대기
                retries=3,  # 실패 시 재시도 횟수
                compression_type='gzip',  # 압축 적용
                batch_size=16384,  # 배치 크기 (16KB)
                linger_ms=10,  # 배치 전송 대기 시간 (10ms)
                max_request_size=1048576  # 최대 요청 크기 (1MB)
            )

            self.topic = settings.KAFKA_TOPIC_ALERT

            logger.info(
                f"AlertProducer 초기화 완료: "
                f"servers={settings.kafka_servers_list}, "
                f"topic={self.topic}"
            )

        except Exception as e:
            logger.error(f"Kafka Producer 초기화 실패: {e}")
            raise

    def send_anomaly_alert(
        self,
        machine_id: int,
        alert_data: Dict[str, Any]
    ) -> bool:
        """
        이상 탐지 알림 발행

        Args:
            machine_id: 머신 ID (파티션 키로 사용)
            alert_data: 알림 데이터 딕셔너리

        Returns:
            성공 여부
        """
        try:
            # machine_id를 키로 사용하여 동일 머신의 메시지는 같은 파티션으로
            key = str(machine_id)

            # 비동기 전송
            future = self.producer.send(
                self.topic,
                key=key,
                value=alert_data
            )

            # 콜백 등록
            future.add_callback(self._on_send_success)
            future.add_errback(self._on_send_error)

            logger.debug(
                f"이상 알림 발행 요청: machine_id={machine_id}, "
                f"topic={self.topic}"
            )

            return True

        except Exception as e:
            logger.error(f"이상 알림 발행 실패: {e}")
            return False

    def send_anomaly_alert_sync(
        self,
        machine_id: int,
        alert_data: Dict[str, Any],
        timeout: int = 10
    ) -> bool:
        """
        이상 탐지 알림 발행 (동기 방식)

        Args:
            machine_id: 머신 ID
            alert_data: 알림 데이터 딕셔너리
            timeout: 타임아웃 (초)

        Returns:
            성공 여부
        """
        try:
            key = str(machine_id)

            # 동기 전송 (블로킹)
            future = self.producer.send(
                self.topic,
                key=key,
                value=alert_data
            )

            # 전송 완료 대기
            record_metadata = future.get(timeout=timeout)

            logger.info(
                f"이상 알림 전송 완료: "
                f"topic={record_metadata.topic}, "
                f"partition={record_metadata.partition}, "
                f"offset={record_metadata.offset}"
            )

            return True

        except KafkaError as e:
            logger.error(f"Kafka 전송 실패: {e}")
            return False
        except Exception as e:
            logger.error(f"이상 알림 전송 실패: {e}")
            return False

    def _on_send_success(self, record_metadata):
        """전송 성공 콜백"""
        logger.info(
            f"메시지 전송 성공: "
            f"{record_metadata.topic}:"
            f"{record_metadata.partition}:"
            f"{record_metadata.offset}"
        )

    def _on_send_error(self, excp):
        """전송 실패 콜백"""
        logger.error(f"메시지 전송 실패: {excp}")

    def flush(self, timeout: Optional[int] = None) -> None:
        """
        모든 대기 중인 메시지 강제 전송

        Args:
            timeout: 타임아웃 (초), None이면 무제한 대기
        """
        try:
            self.producer.flush(timeout=timeout)
            logger.debug("Producer flush 완료")
        except Exception as e:
            logger.error(f"Producer flush 실패: {e}")

    def get_metrics(self) -> Dict[str, Any]:
        """
        Producer 메트릭 조회

        Returns:
            메트릭 딕셔너리
        """
        try:
            metrics = self.producer.metrics()
            return {
                'record_send_rate': metrics.get('record-send-rate', {}).get('metric-value'),
                'record_error_rate': metrics.get('record-error-rate', {}).get('metric-value'),
                'request_latency_avg': metrics.get('request-latency-avg', {}).get('metric-value')
            }
        except Exception as e:
            logger.error(f"메트릭 조회 실패: {e}")
            return {}

    def close(self, timeout: int = 30) -> None:
        """
        Producer 종료

        Args:
            timeout: 종료 타임아웃 (초)
        """
        try:
            self.producer.close(timeout=timeout)
            logger.info("AlertProducer 종료 완료")
        except Exception as e:
            logger.error(f"AlertProducer 종료 실패: {e}")


# 모듈 수준 싱글톤 인스턴스 (지연 로딩)
_producer_instance: Optional[AlertProducer] = None


def get_alert_producer() -> AlertProducer:
    """
    싱글톤 Producer 인스턴스 반환

    Returns:
        초기화된 AlertProducer 인스턴스
    """
    global _producer_instance
    if _producer_instance is None:
        _producer_instance = AlertProducer()
    return _producer_instance