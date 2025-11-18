package dev.study.portal.service;

import dev.study.portal.dto.sensor.SensorDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kafka 센서 데이터 컨슈머
 *
 * <p>책임:</p>
 * <ul>
 *   <li>1. Kafka 토픽(sensor-raw-data)에서 센서 데이터 수신</li>
 *   <li>2. DB 부하 최적화를 위한 배치 버퍼링 (10초 또는 50개 단위)</li>
 *   <li>3. MachineSensorDataService를 통한 영속화 위임</li>
 * </ul>
 *
 * <p>버퍼링 전략:</p>
 * <ul>
 *   <li>크기 기반: 50개 데이터 수집 시 즉시 플러시</li>
 *   <li>시간 기반: 10초마다 스케줄러가 남은 데이터 플러시</li>
 * </ul>
 *
 * <p>주의: 버퍼링은 Consumer의 성능 최적화 수단이며,
 * 실제 비즈니스 로직(영속화)은 MachineSensorDataService에 위임</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final MachineSensorDataService sensorDataService;

    // 배치 버퍼 (Thread-safe)
    private final List<SensorDataDto> batchBuffer =
        Collections.synchronizedList(new ArrayList<>());

    // 배치 설정
    private static final int BATCH_SIZE = 50;           // 50개 단위 배치
    private static final int BATCH_TIMEOUT_MS = 10000;  // 10초 타임아웃

    /**
     * Kafka에서 센서 데이터 수신
     *
     * @param sensorData 센서 데이터 DTO
     */
    @KafkaListener(
        topics = "${kafka.topic.sensor-raw-data}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeSensorData(SensorDataDto sensorData) {
        log.debug("📥 [Kafka Consumer] 메시지 수신 - Machine: {}, DCP: {}",
                sensorData.getMachineId(), sensorData.getDcpConfigId());

        // 버퍼에 추가
        batchBuffer.add(sensorData);

        // 배치 크기에 도달하면 즉시 저장
        if (batchBuffer.size() >= BATCH_SIZE) {
            log.info("📊 [Kafka Consumer] 배치 크기 도달 - 플러시 실행 (Count: {})", batchBuffer.size());
            flushBatch();
        }
    }

    /**
     * 주기적으로 버퍼 플러시 (타임아웃 처리)
     *
     * <p>10초마다 실행되어 버퍼에 남아있는 데이터를 강제로 저장합니다.
     * 이는 데이터 수집 빈도가 낮을 때 데이터가 버퍼에 오래 머무르는 것을 방지합니다.</p>
     */
    @Scheduled(fixedDelay = BATCH_TIMEOUT_MS)
    public void scheduledFlush() {
        if (!batchBuffer.isEmpty()) {
            log.info("⏰ [Kafka Consumer] 타임아웃 도달 - 스케줄러 플러시 실행 (Count: {})",
                    batchBuffer.size());
            flushBatch();
        }
    }

    /**
     * 버퍼의 데이터를 DB에 배치 저장
     *
     * <p>버퍼의 모든 데이터를 복사하고 버퍼를 비운 후,
     * MachineSensorDataService를 통해 DB에 배치 저장합니다.</p>
     *
     * <p>동시성 제어: synchronized를 통해 여러 스레드가 동시에 플러시하는 것을 방지</p>
     */
    private synchronized void flushBatch() {
        if (batchBuffer.isEmpty()) {
            return;
        }

        try {
            // 현재 버퍼 내용을 복사하고 버퍼 클리어
            List<SensorDataDto> dataToSave = new ArrayList<>(batchBuffer);
            batchBuffer.clear();

            log.info("🚀 [Kafka Consumer] 배치 저장 시작 - Count: {}", dataToSave.size());

            // MachineSensorDataService에 영속화 위임
            sensorDataService.saveSensorDataBatch(dataToSave);

            log.info("✅ [Kafka Consumer] 배치 저장 완료 - Count: {}", dataToSave.size());

        } catch (Exception e) {
            log.error("❌ [Kafka Consumer] 배치 저장 실패 - Error: {}", e.getMessage(), e);
            // 실패 시 재처리 로직 추가 가능 (DLQ 전송, 재시도 등)
            // 현재는 로깅만 수행하고 데이터는 손실됨
        }
    }

    /**
     * 버퍼 상태 조회 (모니터링용)
     *
     * @return 현재 버퍼에 쌓인 데이터 개수
     */
    public int getBufferSize() {
        return batchBuffer.size();
    }
}
