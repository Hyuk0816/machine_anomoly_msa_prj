package dev.study.portal.dto.anomalyHistory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyAlertMessage {

    @JsonProperty("machine_id")
    private Long machineId;

    @JsonProperty("sensor_data")
    private EventMessageSensorData eventMessageSensorData;

    @JsonProperty("prediction")
    private Prediction prediction;

    @JsonProperty("detected_at")
    private String detectedAt;
}
