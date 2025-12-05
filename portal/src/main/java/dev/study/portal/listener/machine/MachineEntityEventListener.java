package dev.study.portal.listener.machine;

import dev.study.portal.entity.dcp.DcpConfig;
import dev.study.portal.entity.machine.Machine;
import dev.study.portal.event.machine.MachineCreatedEvent;
import dev.study.portal.event.machine.MachineDeletedEvent;
import dev.study.portal.event.machine.MachineModifyEvent;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@Getter
@Slf4j
@RequiredArgsConstructor
public class MachineEntityEventListener {
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostPersist
    public void onPostPersist(Machine machine) {
        log.info("Machine 생성 감지 - ID: {}, Name{}",
                machine.getId(), machine.getName());

        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new MachineCreatedEvent(
                    this,
                    machine.getId(),
                    machine.getName()
            ));
        }
    }

    @PostUpdate
    public void onPostUpdate(Machine machine) {
        log.info("Machine 수정 감지 - ID: {}, Name{}",
                machine.getId(), machine.getName());
        if(applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new MachineModifyEvent(
                    this,
                    machine.getId(),
                    machine.getName()
            ));
        }
    }



    @PostRemove
    public void onPostRemove(Machine machine) {
        log.info("Machine 삭제 감지 - ID: {}, Name{}",
                machine.getId(), machine.getName());

        if(applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new MachineDeletedEvent(
                    this,
                    machine.getName()
            ));
        }
    }

}
