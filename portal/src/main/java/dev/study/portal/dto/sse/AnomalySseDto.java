package dev.study.portal.dto.sse;

import dev.study.portal.dto.anomalyHistory.AnomalyAlertMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnomalySseDto {
    private Long machineId;
    private String machineName;      // 설비 이름
    private LocalDateTime detectedAt; // 발생 시간
    private String severity;          // 심각도 (WARNING, ALERT, CRITICAL)
    private Double anomalyProbability;

    public static AnomalySseDto from(
            AnomalyAlertMessage message,
            String machineName
    ) {
        return AnomalySseDto.builder()
                .machineId(message.getMachineId())
                .machineName(machineName)
                .detectedAt(LocalDateTime.parse(message.getDetectedAt()))
                .severity(message.getPrediction().getSeverity())
                .anomalyProbability(message.getPrediction().getAnomalyProbability())
                .build();
    }
}
