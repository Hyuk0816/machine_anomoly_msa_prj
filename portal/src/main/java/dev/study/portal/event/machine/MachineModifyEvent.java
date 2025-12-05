package dev.study.portal.event.machine;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class MachineModifyEvent extends ApplicationEvent {
    private final Long machineId;
    private final String machineName;

    public MachineModifyEvent(Object source, Long machineId, String machineName) {
        super(source);
        this.machineId = machineId;
        this.machineName = machineName;
    }
}
