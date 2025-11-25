package dev.study.portal.service;

import dev.study.portal.dto.sensor.SensorDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveDataCollectorService {

    private final WebClient webClient;

    /**
     * 외부 API에서 센서 데이터 수집
     * @param dcpConfigId DcpConfig ID
     * @param machineId Machine ID
     * @param apiEndpoint API 엔드포인트
     * @param apiMethod HTTP 메소드 (GET, POST 등)
     * @return 수집된 센서 데이터
     */
    public Mono<SensorDataDto> collectData(Long dcpConfigId, Long machineId,
                                            String apiEndpoint, String apiMethod) {

        log.info("🌐 [DataCollector] API 호출 시작 - Machine: {}, Endpoint: {}", machineId, apiEndpoint);

        HttpMethod method = apiMethod != null ? HttpMethod.valueOf(apiMethod) : HttpMethod.GET;

        return webClient
                .method(method)
                .uri(apiEndpoint)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(10))
                        .doBeforeRetry(retrySignal ->
                                log.warn("⚠️ [DataCollector] API 호출 재시도 - 시도 횟수: {}",
                                        retrySignal.totalRetries() + 1)))
                .map(responseData -> mapToSensorData(dcpConfigId, machineId, responseData))
                .doOnSuccess(data -> log.info("✅ [DataCollector] 데이터 수집 성공 - Machine: {}", machineId))
                .doOnError(error -> log.error("❌ [DataCollector] 데이터 수집 실패 - Machine: {}, Error: {}",
                        machineId, error.getMessage()));
    }

    /**
     * API 응답 데이터를 SensorDataDto로 변환
     */
    private SensorDataDto mapToSensorData(Long dcpConfigId, Long machineId, Map<String, Object> responseData) {
        return SensorDataDto.builder()
                .dcpConfigId(dcpConfigId)
                .machineId(machineId)
                .airTemperature(getDoubleValue(responseData, "airTemperature"))
                .processTemperature(getDoubleValue(responseData, "processTemperature"))
                .rotationalSpeed(getIntegerValue(responseData, "rotationalSpeed"))
                .torque(getDoubleValue(responseData, "torque"))
                .toolWear(getIntegerValue(responseData, "toolWear"))
                .collectedAt(LocalDateTime.now())
                .build();
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        // 문자열인 경우 파싱 시도
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                log.warn("[DataCollector] Double 변환 실패 - key: {}, value: {}", key, value);
                return null;
            }
        }

        return null;
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        // 문자열인 경우 파싱 시도
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("[DataCollector] Integer 변환 실패 - key: {}, value: {}", key, value);
                return null;
            }
        }

        return null;
    }
}