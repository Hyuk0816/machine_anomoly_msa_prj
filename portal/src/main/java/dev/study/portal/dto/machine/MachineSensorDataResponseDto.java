package dev.study.portal.dto.machine;

import dev.study.portal.entity.machine.MachineSensorData;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MachineSensorDataResponseDto {
    private Long id;
    private Long machineId;
    private Double airTemperature;
    private Double processTemperature;
    private Integer rotationalSpeed;
    private Double torque;
    private Integer toolWear;
    private LocalDateTime createdAt;

    public static MachineSensorDataResponseDto from(MachineSensorData machineSensorData) {
        return MachineSensorDataResponseDto.builder()
                .id(machineSensorData.getId())
                .machineId(machineSensorData.getMachineId())
                .airTemperature(machineSensorData.getAirTemperature())
                .processTemperature(machineSensorData.getProcessTemperature())
                .rotationalSpeed(machineSensorData.getRotationalSpeed())
                .torque(machineSensorData.getTorque())
                .toolWear(machineSensorData.getToolWear())
                .createdAt(machineSensorData.getCreatedAt())
                .build();
    }
}
