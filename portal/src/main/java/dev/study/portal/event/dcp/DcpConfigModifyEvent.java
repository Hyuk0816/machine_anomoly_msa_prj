package dev.study.portal.event.dcp;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class DcpConfigModifyEvent extends ApplicationEvent {
    private final Long dcpConfigId;
    private final Long machineId;
    private final Integer collectInterval;
    private final String apiEndpoint;

    public DcpConfigModifyEvent(Object source, Long dcpConfigId, Long machineId, Integer collectInterval, String apiEndpoint) {
        super(source);
        this.dcpConfigId = dcpConfigId;
        this.machineId = machineId;
        this.collectInterval = collectInterval;
        this.apiEndpoint = apiEndpoint;
    }
}
