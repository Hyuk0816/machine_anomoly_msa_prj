package dev.study.portal.listener.anomoly;

import dev.study.portal.dto.anomalyHistory.AnomalyAlertMessage;
import dev.study.portal.service.AnomalyHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AnomalyAlertListener {

    private final AnomalyHistoryService anomalyHistoryService;

    @KafkaListener(
            topics = "${kafka.topic.anomaly-alerts}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    private void handleAnomalyAlert(@Payload AnomalyAlertMessage message) {
        log.info("🚨 이상 탐지 알림 수신: machineId={}", message.getMachineId());
        anomalyHistoryService.saveFromKafkaMessage(message);
    }
}
