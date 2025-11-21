package dev.study.portal.service;

import dev.study.portal.dto.machine.MachineSensorDataResponseDto;
import dev.study.portal.dto.sensor.SensorDataDto;
import dev.study.portal.entity.machine.MachineSensorData;
import dev.study.portal.repository.sensor.MachineSensorDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MachineSensorDataService {

    private final MachineSensorDataRepository sensorDataRepository;

    /**
     * 센서 데이터 단건 저장
     * @param dto 센서 데이터 DTO
     * @return 저장된 엔티티
     */
    @Transactional
    public MachineSensorData saveSensorData(SensorDataDto dto) {
        log.debug("💾 [Sensor Data Service] 센서 데이터 저장 - Machine: {}", dto.getMachineId());

        MachineSensorData entity = MachineSensorData.builder()
                .machineId(dto.getMachineId())
                .airTemperature(dto.getAirTemperature())
                .processTemperature(dto.getProcessTemperature())
                .rotationalSpeed(dto.getRotationalSpeed())
                .torque(dto.getTorque())
                .toolWear(dto.getToolWear())
                .build();

        return sensorDataRepository.save(entity);
    }

    /**
     * 센서 데이터 배치 저장 (성능 최적화)
     * @param dtoList 센서 데이터 DTO 리스트
     */
    @Transactional
    public void saveSensorDataBatch(List<SensorDataDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            log.warn("⚠️ [Sensor Data Service] 배치 저장 요청이 비어있습니다.");
            return;
        }

        log.info("💾 [Sensor Data Service] 배치 저장 시작 - Count: {}", dtoList.size());

        List<MachineSensorData> entities = dtoList.stream()
                .map(dto -> MachineSensorData.builder()
                        .machineId(dto.getMachineId())
                        .airTemperature(dto.getAirTemperature())
                        .processTemperature(dto.getProcessTemperature())
                        .rotationalSpeed(dto.getRotationalSpeed())
                        .torque(dto.getTorque())
                        .toolWear(dto.getToolWear())
                        .build())
                .collect(Collectors.toList());

        sensorDataRepository.saveAll(entities);

        log.info("✅ [Sensor Data Service] 배치 저장 완료 - Count: {}", entities.size());
    }

    /**
     * 특정 설비의 최근 센서 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<MachineSensorDataResponseDto> getSensorDataCreatedAtBetween(Long machineId, LocalDateTime startAt, LocalDateTime endAt) {
        return sensorDataRepository.findByMachineIdAndCreatedAtBetween(machineId, startAt, endAt)
                .stream()
                .map(MachineSensorDataResponseDto::from)
                .collect(Collectors.toList());
    }
}