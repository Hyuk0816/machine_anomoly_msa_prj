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
public class EventMessageSensorData {
    @JsonProperty("airTemperature")
    private Double airTemperature;

    @JsonProperty("processTemperature")
    private Double processTemperature;

    @JsonProperty("rotationalSpeed")
    private Integer rotationalSpeed;

    @JsonProperty("torque")
    private Double torque;

    @JsonProperty("toolWear")
    private Integer toolWear;
}
