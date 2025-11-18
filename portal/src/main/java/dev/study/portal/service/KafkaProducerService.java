package dev.study.portal.service;

import dev.study.portal.dto.sensor.SensorDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, SensorDataDto> kafkaTemplate;

    @Value("${kafka.topic.sensor-raw-data}")
    private String sensorDataTopic;

    /**
     * 센서 데이터를 Kafka에 발행
     * @param sensorData 센서 데이터
     * @return 발행 결과를 담은 Mono
     */
    public Mono<Void> sendSensorData(SensorDataDto sensorData) {
        String partitionKey = String.valueOf(sensorData.getMachineId());

        log.info("📤 [Kafka Producer] 센서 데이터 전송 시작 - Machine: {}, DCP: {}",
                sensorData.getMachineId(), sensorData.getDcpConfigId());

        return Mono.fromFuture(sendAsync(partitionKey, sensorData))
                .doOnSuccess(result -> {
                    var metadata = result.getRecordMetadata();
                    log.info("✅ [Kafka Producer] 전송 성공 - Topic: {}, Partition: {}, Offset: {}, Machine: {}",
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset(),
                            sensorData.getMachineId());
                })
                .doOnError(error -> log.error("❌ [Kafka Producer] 전송 실패 - Machine: {}, Error: {}",
                        sensorData.getMachineId(), error.getMessage()))
                .then();
    }

    /**
     * 비동기로 Kafka에 메시지 전송
     */
    private CompletableFuture<SendResult<String, SensorDataDto>> sendAsync(String key, SensorDataDto data) {
        return kafkaTemplate.send(sensorDataTopic, key, data);
    }
}