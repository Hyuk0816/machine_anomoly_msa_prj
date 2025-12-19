package dev.study.portal.event.handler;

import dev.study.portal.event.machine.MachineCreatedEvent;
import dev.study.portal.event.machine.MachineDeletedEvent;
import dev.study.portal.event.machine.MachineModifyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MachineEventHandler {
    private final RedisTemplate<String, String> redisTemplate;
    private final String MACHINE_NAME_PREFIX = "machine:name:";
    private final String MACHINE_ID_PREFIX = "machine:id:";

    @Async
    @EventListener
    public void handleCreated(MachineCreatedEvent event) {
        log.info("[Event Handler] Machine 생성 이벤트 수신 - ID: {}", event.getMachineId());
        redisTemplate.opsForValue().set(MACHINE_NAME_PREFIX + event.getMachineName(), String.valueOf(event.getMachineId()));

        //수정했을 시 ID를 기준으로 원래 이름을 가져와 삭제해야하니 역방향 매핑 추가 machine:id:{id} -> previousName
        redisTemplate.opsForValue().set(MACHINE_ID_PREFIX + event.getMachineId(), event.getMachineName());
    }

    @Async
    @EventListener
    public void handleModified(MachineModifyEvent event) {
        log.info("[Event Handler] Machine 수정 이벤트 수신 - ID: {}, 이름: {}", event.getMachineId(), event.getMachineName());
        //이전 이름을 가져와서 삭제
        String previousNameKey = MACHINE_ID_PREFIX + event.getMachineId();
        String previousName = redisTemplate.opsForValue().get(previousNameKey);

        if (previousName != null && previousName.equals(event.getMachineName())) {
            log.debug("[Redis] Machine 이름 변경 없음 - ID: {}, Name: {}",
                    event.getMachineId(), event.getMachineName());
            return; // 이름이 같으면 아무것도 안 함
        }
        if (previousName != null) {
            redisTemplate.opsForValue().getAndDelete(MACHINE_NAME_PREFIX + previousName);
        }
        //새로운 이름으로 추가
        redisTemplate.opsForValue().set(MACHINE_NAME_PREFIX + event.getMachineName(), String.valueOf(event.getMachineId()));
        //역방향도 새로운 이름으로 값 변경
        redisTemplate.opsForValue().set(MACHINE_ID_PREFIX + event.getMachineId(), event.getMachineName());
    }

    @Async
    @EventListener
    public void handleDeleted(MachineDeletedEvent event) {
        log.info("[Event Handler] Machine 삭제 이벤트 수신 - 이름: {}", event.getMachineName());
        String machineId = redisTemplate.opsForValue().get(MACHINE_NAME_PREFIX + event.getMachineName());
        redisTemplate.delete(MACHINE_NAME_PREFIX + event.getMachineName());
        //역방향 인덱스도 함께 삭제
        redisTemplate.delete(MACHINE_ID_PREFIX + machineId);
    }

}
