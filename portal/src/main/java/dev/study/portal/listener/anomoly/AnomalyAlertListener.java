package dev.study.portal.listener.anomoly;

import dev.study.portal.common.exception.machine.MachineNotFoundException;
import dev.study.portal.dto.anomalyHistory.AnomalyAlertMessage;
import dev.study.portal.dto.sse.AnomalySseDto;
import dev.study.portal.entity.machine.Machine;
import dev.study.portal.repository.machine.MachineRepository;
import dev.study.portal.service.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnomalyAlertListener {

    private final SseEmitterService sseEmitterService;
    private final MachineRepository machineRepository;

    @KafkaListener(
            topics = "${kafka.topic.anomaly-alerts}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    private void handleAnomalyAlert(@Payload AnomalyAlertMessage message) {
        log.info("[Kafka] Anomaly alert received - machineId={}", message.getMachineId());

        // Machine 조회 (SSE 메시지에 machineName 포함을 위해)
        Machine machine = machineRepository.findById(message.getMachineId())
                .orElseThrow(MachineNotFoundException::new);

        // SSE 브로드캐스트만 수행 (AnomalyHistory 저장은 AI Server에서 처리)
        AnomalySseDto sseMessage = AnomalySseDto.from(message, machine.getName());
        sseEmitterService.broadcast(sseMessage);

        log.info("[SSE] Anomaly alert broadcasted - machineId={}, machineName={}",
                message.getMachineId(), machine.getName());
    }
}
