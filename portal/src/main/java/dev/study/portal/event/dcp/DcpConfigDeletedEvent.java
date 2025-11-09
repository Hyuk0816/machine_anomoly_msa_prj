package dev.study.portal.event.dcp;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class DcpConfigDeletedEvent extends ApplicationEvent {
    private final Long dcpConfigId;

    public DcpConfigDeletedEvent(Object source, Long dcpConfigId) {
        super(source);
        this.dcpConfigId = dcpConfigId;
    }
}
