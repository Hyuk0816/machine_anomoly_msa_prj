"""
Kafka Consumer 모듈

sensor-raw-data 토픽에서 센서 데이터를 수신하고
실시간 이상 탐지를 수행합니다.
"""
from kafka import KafkaConsumer
from kafka.errors import KafkaError
import json
import logging
from typing import Optional
import signal

from ..config import settings
from ..ml.predictor import get_predictor
from ..cache.machine_cache import get_machine_cache
from ..db.repositories import get_outbox_repository
from ..db.models import Outbox
from .producer import get_alert_producer

logger = logging.getLogger(__name__)


class SensorDataConsumer:
    """
    센서 데이터 Consumer

    sensor-raw-data 토픽에서 데이터를 수신하여
    실시간 이상 탐지를 수행하고 결과를 Outbox에 저장합니다.
    """

    def __init__(self):
        """Kafka Consumer 및 의존성 초기화"""
        try:
            # Kafka Consumer 생성
            self.consumer = KafkaConsumer(
                settings.KAFKA_TOPIC_SENSOR,
                bootstrap_servers=settings.kafka_servers_list,
                group_id=settings.KAFKA_GROUP_ID,
                value_deserializer=lambda m: json.loads(m.decode('utf-8')),
                key_deserializer=lambda k: k.decode('utf-8') if k else None,
                auto_offset_reset='latest',  # 최신 메시지부터 소비
                enable_auto_commit=True,  # 자동 오프셋 커밋
                auto_commit_interval_ms=5000,  # 5초마다 커밋
                max_poll_records=100,  # 한 번에 최대 100개 메시지
                session_timeout_ms=30000,  # 30초
                heartbeat_interval_ms=10000  # 10초
            )

            # 의존성 주입 (싱글톤 인스턴스들)
            self.predictor = get_predictor()
            self.cache = get_machine_cache()  # 캐시 미스 시 자동 DB 조회
            self.repository = get_outbox_repository()
            self.alert_producer = get_alert_producer()

            # 실행 플래그
            self.running = False

            logger.info(
                f"SensorDataConsumer 초기화 완료: "
                f"topic={settings.KAFKA_TOPIC_SENSOR}, "
                f"group={settings.KAFKA_GROUP_ID}"
            )

        except Exception as e:
            logger.error(f"Kafka Consumer 초기화 실패: {e}")
            raise

    def start(self) -> None:
        """
        Consumer 시작 및 메시지 처리 루프

        SIGINT (Ctrl+C)로 안전하게 종료할 수 있습니다.
        """
        # 시그널 핸들러 등록
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)

        self.running = True
        logger.info("SensorDataConsumer 시작...")

        try:
            # 메시지 폴링 루프
            for message in self.consumer:
                if not self.running:
                    break

                try:
                    self._process_message(message)
                except Exception as e:
                    logger.error(
                        f"메시지 처리 중 오류 발생: {e}, "
                        f"offset={message.offset}"
                    )
                    # 개별 메시지 처리 실패는 로깅만 하고 계속 진행

        except KafkaError as e:
            logger.error(f"Kafka 오류 발생: {e}")
        finally:
            self.stop()

    def _process_message(self, message) -> None:
        """
        개별 메시지 처리 파이프라인

        Args:
            message: Kafka 메시지
        """
        # 1단계: 메시지 파싱
        sensor_data = message.value
        machine_id = sensor_data.get('machineId')

        if machine_id is None:
            logger.warning("machineId가 없는 메시지 수신")
            return

        logger.debug(
            f"센서 데이터 수신: machine_id={machine_id}, "
            f"partition={message.partition}, "
            f"offset={message.offset}"
        )

        # 2단계: 머신 타입 조회
        # cache.get_machine_type()는 캐시 미스 시 자동으로 Portal DB 조회
        machine_type = self.cache.get_machine_type(machine_id)

        if machine_type is None:
            logger.warning(
                f"머신 타입을 찾을 수 없음: machine_id={machine_id}. "
                f"Portal DB에 해당 머신이 존재하지 않습니다."
            )
            return

        # 3단계: 이상 탐지 예측
        try:
            prediction_result = self.predictor.predict(
                sensor_data=sensor_data,
                machine_type=machine_type,
                return_probabilities=True
            )

            # 4단계: 이상 감지 시 처리
            if prediction_result['is_anomaly']:
                self._handle_anomaly(machine_id, sensor_data, prediction_result)
            else:
                logger.debug(
                    f"정상 데이터: machine_id={machine_id}, "
                    f"normal_prob={prediction_result.get('normal_probability', 0):.4f}"
                )

        except Exception as e:
            logger.error(f"예측 처리 실패 (machine_id={machine_id}): {e}")

    def _handle_anomaly(
        self,
        machine_id: int,
        sensor_data: dict,
        prediction_result: dict
    ) -> None:
        """
        이상 탐지 시 처리 로직

        1. Outbox에 이벤트 저장 → Debezium CDC가 감지 → Kafka 발행
        2. (선택적) 직접 Kafka로도 알림 발행 가능

        Args:
            machine_id: 머신 ID
            sensor_data: 센서 데이터
            prediction_result: 예측 결과
        """
        logger.warning(
            f"🚨 이상 탐지! machine_id={machine_id}, "
            f"anomaly_prob={prediction_result.get('anomaly_probability', 0):.4f}"
        )

        try:
            # Outbox 이벤트 생성
            outbox_event = Outbox.create_anomaly_event(
                machine_id=machine_id,
                sensor_data=sensor_data,
                prediction_result=prediction_result
            )

            # DB에 저장 (Debezium CDC가 이를 감지하여 Kafka로 발행)
            saved_event = self.repository.save_event(outbox_event)

            logger.info(
                f"Outbox 이벤트 저장 완료: "
                f"id={saved_event.id}, "
                f"aggregate_id={saved_event.aggregate_id}"
            )

            # (선택적) 즉시 Kafka로도 발행
            # Debezium과 별개로 실시간 알림이 필요한 경우 아래 주석 해제
            # self.alert_producer.send_anomaly_alert(
            #     machine_id=machine_id,
            #     alert_data=outbox_event.payload
            # )

        except Exception as e:
            logger.error(f"이상 탐지 이벤트 저장 실패: {e}")

    def _signal_handler(self, signum, frame):
        """시그널 핸들러 (Ctrl+C, SIGTERM)"""
        logger.info(f"시그널 수신: {signum}, Consumer 종료 중...")
        self.running = False

    def stop(self) -> None:
        """Consumer 안전 종료"""
        logger.info("SensorDataConsumer 종료 중...")
        self.running = False

        try:
            # Consumer 종료
            self.consumer.close()
            logger.info("Kafka Consumer 종료 완료")

            # 리소스 정리
            self.alert_producer.close()
            self.cache.close()
            self.repository.close()

        except Exception as e:
            logger.error(f"Consumer 종료 중 오류: {e}")

    def get_metrics(self) -> dict:
        """
        Consumer 메트릭 조회

        Returns:
            메트릭 딕셔너리
        """
        try:
            metrics = self.consumer.metrics()
            return {
                'records_consumed_rate': metrics.get('records-consumed-rate', {}).get('metric-value'),
                'fetch_latency_avg': metrics.get('fetch-latency-avg', {}).get('metric-value'),
                'commit_latency_avg': metrics.get('commit-latency-avg', {}).get('metric-value')
            }
        except Exception as e:
            logger.error(f"메트릭 조회 실패: {e}")
            return {}


# Consumer 실행 함수
def run_consumer():
    """Consumer를 실행하는 메인 함수"""
    consumer = SensorDataConsumer()
    consumer.start()


if __name__ == "__main__":
    # 로깅 설정
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )

    # Consumer 실행
    run_consumer()
