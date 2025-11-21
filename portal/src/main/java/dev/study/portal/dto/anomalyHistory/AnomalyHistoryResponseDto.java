package dev.study.portal.dto.anomalyHistory;

import dev.study.portal.entity.anomalyHistory.AnomalyHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AnomalyHistoryResponseDto {
    private Long id;
    private Long machineId;
    private String detectedAt;
    private Double anomalyProbability;
    private EventMessageSensorData eventMessageSensorData;

    public static AnomalyHistoryResponseDto from(AnomalyHistory anomalyHistory, EventMessageSensorData eventMessageSensorData) {



        return AnomalyHistoryResponseDto.builder()
                .id(anomalyHistory.getId())
                .machineId(anomalyHistory.getMachine().getId())
                .detectedAt(anomalyHistory.getDetectedAt().toString())
                .anomalyProbability(anomalyHistory.getAnomalyProbability())
                .eventMessageSensorData(eventMessageSensorData)
                .build();
    }
}
