package dev.study.portal.scheduler;

import dev.study.portal.entity.dcp.DcpConfig;
import dev.study.portal.repository.dcp.DcpConfigRepository;
import dev.study.portal.service.KafkaProducerService;
import dev.study.portal.service.ReactiveDataCollectorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * DCP 설정 기반 동적 스케줄러 관리자
 *
 * <p>책임:</p>
 * <ul>
 *   <li>1. DcpConfig 이벤트에 따라 스케줄러 동적 생성/수정/삭제</li>
 *   <li>2. 각 DcpConfig별 독립적인 데이터 수집 스케줄 실행</li>
 *   <li>3. ReactiveDataCollectorService를 통한 API 데이터 수집</li>
 *   <li>4. KafkaProducerService를 통한 수집 데이터 발행</li>
 * </ul>
 *
 * <p>동작 흐름:</p>
 * <pre>
 * DcpConfig 생성 이벤트
 *   → createScheduler()
 *   → TaskScheduler에 주기적 작업 등록
 *   → [주기마다] API 호출 → Kafka 발행
 * </pre>
 */
@Slf4j
@Component("dynamicSchedulerManager")
@RequiredArgsConstructor
public class DcpConfigScheduler {
    private final TaskScheduler taskScheduler;
    private final ReactiveDataCollectorService dataCollectorService;
    private final KafkaProducerService kafkaProducerService;
    private final DcpConfigRepository dcpConfigRepository;

    // 각 DcpConfig ID별로 ScheduledFuture를 저장
    private final Map<Long, ScheduledFuture<?>> schedulerMap = new ConcurrentHashMap<>();

    /**
     * 애플리케이션 시작 시 DB에 저장된 모든 DCP 설정을 스케줄러에 등록
     *
     * 재시작 시에도 기존 DCP 설정들이 자동으로 스케줄러에 등록되어
     * 데이터 수집이 중단 없이 계속 진행됩니다.
     */
    @PostConstruct
    public void initializeSchedulers() {
        log.info("[Scheduler Init] 애플리케이션 시작 - 기존 DCP 설정 스케줄러 초기화 시작");

        try {
            // DB에서 모든 DCP 설정 조회
            List<DcpConfig> allConfigs = dcpConfigRepository.findAll();

            if (allConfigs.isEmpty()) {
                log.info("[Scheduler Init] 등록된 DCP 설정이 없습니다.");
                return;
            }

            log.info("[Scheduler Init] 총 {}개의 DCP 설정 발견", allConfigs.size());

            // 각 DCP 설정에 대해 스케줄러 생성
            int successCount = 0;
            int failCount = 0;

            for (DcpConfig config : allConfigs) {
                try {
                    createScheduler(
                            config.getId(),
                            config.getMachine().getId(),
                            config.getCollectInterval(),
                            config.getApiEndpoint()
                    );
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("[Scheduler Init] DCP 설정 스케줄러 생성 실패 - DcpConfig ID: {}, Error: {}",
                            config.getId(), e.getMessage(), e);
                }
            }

            log.info("[Scheduler Init] 스케줄러 초기화 완료 - 성공: {}, 실패: {}, 전체: {}",
                    successCount, failCount, allConfigs.size());

        } catch (Exception e) {
            log.error("[Scheduler Init] 스케줄러 초기화 중 예외 발생", e);
        }
    }

    /**
     * 스케줄러 생성
     * @param dcpConfigId DcpConfig ID
     * @param machineId 설비 ID
     * @param intervalSeconds 수집 주기 (초)
     * @param apiEndpoint API 엔드포인트
     */
    public void createScheduler(Long dcpConfigId, Long machineId,
                                Integer intervalSeconds, String apiEndpoint) {

        // 이미 스케줄러가 있으면 중복 생성 방지
        if (schedulerMap.containsKey(dcpConfigId)) {
            log.warn("⚠️ 스케줄러가 이미 존재합니다. DcpConfig ID: {}", dcpConfigId);
            return;
        }

        log.info("✅ 스케줄러 생성 시작 - DcpConfig ID: {}, Interval: {}초", dcpConfigId, intervalSeconds);

        // 주기적으로 실행될 작업 정의
        Runnable task = () -> {
            try {
                log.info("🔄 [Scheduler-{}] 데이터 수집 실행 - Machine: {}, Endpoint: {}",
                        dcpConfigId, machineId, apiEndpoint);

                // 1. ReactiveDataCollectorService를 통해 외부 API에서 센서 데이터 수집
                dataCollectorService.collectData(dcpConfigId, machineId, apiEndpoint, "GET")
                        // 2. 수집된 데이터를 KafkaProducerService를 통해 Kafka에 발행
                        .flatMap(sensorData -> kafkaProducerService.sendSensorData(sensorData))
                        // 3. 성공/실패 로그
                        .doOnSuccess(v -> log.info("✅ [Scheduler-{}] 데이터 수집 및 Kafka 발행 완료 - Machine: {}",
                                dcpConfigId, machineId))
                        .doOnError(error -> log.error("❌ [Scheduler-{}] 데이터 수집 또는 Kafka 발행 실패 - Machine: {}, Error: {}",
                                dcpConfigId, machineId, error.getMessage()))
                        // 4. Reactive Chain 실행 (subscribe)
                        .subscribe();

            } catch (Exception e) {
                log.error("❌ [Scheduler-{}] 데이터 수집 작업 실행 중 예외 발생", dcpConfigId, e);
            }
        };

        // 스케줄러 등록 (fixedDelay 방식)
        ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleWithFixedDelay(
                task,
                Duration.ofSeconds(intervalSeconds)
        );

        // Map에 저장
        schedulerMap.put(dcpConfigId, scheduledFuture);

        log.info("✅ 스케줄러 생성 완료 - DcpConfig ID: {}, 활성 스케줄러 수: {}",
                dcpConfigId, schedulerMap.size());
    }

    /**
     * 스케줄러 수정
     * @param dcpConfigId DcpConfig ID
     * @param machineId 설비 ID
     * @param intervalSeconds 수집 주기 (초)
     * @param apiEndpoint API 엔드포인트
     */
    public void updateScheduler(Long dcpConfigId, Long machineId,
                                Integer intervalSeconds, String apiEndpoint) {

        log.info("🔄 스케줄러 수정 시작 - DcpConfig ID: {}", dcpConfigId);

        // 기존 스케줄러 제거
        removeScheduler(dcpConfigId);

        // 새로운 설정으로 스케줄러 생성
        createScheduler(dcpConfigId, machineId, intervalSeconds, apiEndpoint);

        log.info("✅ 스케줄러 수정 완료 - DcpConfig ID: {}", dcpConfigId);
    }

    /**
     * 스케줄러 삭제
     * @param dcpConfigId DcpConfig ID
     */
    public void removeScheduler(Long dcpConfigId) {
        ScheduledFuture<?> scheduledFuture = schedulerMap.get(dcpConfigId);

        if (scheduledFuture != null) {
            log.info("🗑️ 스케줄러 삭제 시작 - DcpConfig ID: {}", dcpConfigId);

            // 스케줄러 중지
            scheduledFuture.cancel(false);

            // Map에서 제거
            schedulerMap.remove(dcpConfigId);

            log.info("✅ 스케줄러 삭제 완료 - DcpConfig ID: {}, 활성 스케줄러 수: {}",
                    dcpConfigId, schedulerMap.size());
        } else {
            log.warn("⚠️ 삭제할 스케줄러가 없습니다. DcpConfig ID: {}", dcpConfigId);
        }
    }

    /**
     * 모든 활성 스케줄러 조회
     */
    public Map<Long, ScheduledFuture<?>> getAllSchedulers() {
        return Map.copyOf(schedulerMap);
    }

    /**
     * 활성 스케줄러 개수
     */
    public int getActiveSchedulerCount() {
        return schedulerMap.size();
    }
}
