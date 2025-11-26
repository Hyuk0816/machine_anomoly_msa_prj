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
public class Prediction {
    @JsonProperty("is_anomaly")
    private Boolean isAnomaly;

    @JsonProperty("anomaly_probability")
    private Double anomalyProbability;

    @JsonProperty("machine_type")
    private String machineType;

    @JsonProperty("severity")
    private String severity;
}
