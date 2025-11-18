package dev.study.portal.dto.sensor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataDto {
    private Long machineId;
    private Long dcpConfigId;
    private Double airTemperature;
    private Double processTemperature;
    private Integer rotationalSpeed;
    private Double torque;
    private Integer toolWear;
    private Integer target;
    private LocalDateTime collectedAt;
}