package dev.study.portal.service.sse;

import dev.study.portal.dto.sse.AnomalySseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SseEmitterService {
    private final Map<String, SseEmitter> emitters = new HashMap<>();
    private static final Long TIMEOUT = 60L * 1000 * 60; // 60 minutes
    private static final String ANOMALY_ALERT = "anomaly-alert";

    public SseEmitter subscribe(String clientId){
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(clientId, emitter);

        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError((e) -> emitters.remove(clientId));
        return emitter;
    }

    public void broadcast(AnomalySseDto anomalySseDto){
        emitters.forEach((key, emitter) -> {
            try{
                emitter.send(SseEmitter.event()
                        .name(ANOMALY_ALERT)
                        .data(anomalySseDto));
            } catch (IOException e){
                log.error("SSE 전송 실패 {}: {}", key, e.getMessage());
                emitters.remove(key);
            }
        });
    }

}
