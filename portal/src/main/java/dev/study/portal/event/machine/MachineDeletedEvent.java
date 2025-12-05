package dev.study.portal.event.machine;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class MachineDeletedEvent extends ApplicationEvent {
    private final String machineName;

    public MachineDeletedEvent(Object source, String machineName) {
        super(source);
        this.machineName = machineName;
    }
}
