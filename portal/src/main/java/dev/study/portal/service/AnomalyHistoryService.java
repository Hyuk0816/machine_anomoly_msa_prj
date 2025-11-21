package dev.study.portal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.study.portal.common.exception.machine.MachineNotFoundException;
import dev.study.portal.common.exception.sensordata.SensorDataJsonWriteException;
import dev.study.portal.dto.anomalyHistory.AnomalyAlertMessage;
import dev.study.portal.dto.anomalyHistory.AnomalyHistoryResponseDto;
import dev.study.portal.dto.anomalyHistory.EventMessageSensorData;
import dev.study.portal.entity.anomalyHistory.AnomalyHistory;
import dev.study.portal.entity.machine.Machine;
import dev.study.portal.repository.anomalyHistory.AnomalyHistoryRepository;
import dev.study.portal.repository.machine.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnomalyHistoryService{
    private final AnomalyHistoryRepository anomalyHistoryRepository;
    private final MachineRepository machineRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<AnomalyHistoryResponseDto> getAll(){
        return anomalyHistoryRepository.findAll()
                .stream()
                .map(this::getAnomalyHistoryResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnomalyHistoryResponseDto> findByDetectedAtBetween(LocalDateTime startAt, LocalDateTime endAt){
        return anomalyHistoryRepository.findByDetectedAtBetween(startAt, endAt)
                .stream().map(this::getAnomalyHistoryResponseDto)
                .collect(Collectors.toList());
    }


    private AnomalyHistoryResponseDto getAnomalyHistoryResponseDto(AnomalyHistory anomalyHistory) {
        try {
            EventMessageSensorData eventMessageSensorData = objectMapper.readValue(anomalyHistory.getSensorData(), EventMessageSensorData.class);
            return AnomalyHistoryResponseDto.from(anomalyHistory, eventMessageSensorData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Kafka 이벤트 메시지로부터 AnomalyHistory 저장
     *
     * @param message Kafka에서 수신한 이상 탐지 알림 메시지
     * @throws IllegalArgumentException Machine을 찾을 수 없는 경우
     * @throws RuntimeException JSON 직렬화 실패 시
     */
    @Transactional
    public void saveFromKafkaMessage(AnomalyAlertMessage message) {
        // 1. Machine 조회
        Machine machine = machineRepository.findById(message.getMachineId())
                .orElseThrow(MachineNotFoundException::new);

        // 2. Sensor Data를 JSON 문자열로 변환
        String sensorDataJson;
        try {
            sensorDataJson = objectMapper.writeValueAsString(message.getEventMessageSensorData());
        } catch (JsonProcessingException e) {
            throw new SensorDataJsonWriteException(e);
        }

        // 3. ISO-8601 문자열을 LocalDateTime으로 파싱
        LocalDateTime detectedAt = LocalDateTime.parse(message.getDetectedAt());

        // 4. AnomalyHistory 엔티티 생성 및 저장
        AnomalyHistory anomalyHistory = AnomalyHistory.builder()
                .machine(machine)
                .detectedAt(detectedAt)
                .anomalyProbability(message.getPrediction().getAnomalyProbability())
                .sensorData(sensorDataJson)
                .build();

        anomalyHistoryRepository.save(anomalyHistory);
    }
}
