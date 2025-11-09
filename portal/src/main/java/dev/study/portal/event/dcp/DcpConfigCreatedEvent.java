package dev.study.portal.event.dcp;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DcpConfigCreatedEvent extends ApplicationEvent {
    private final Long dcpConfigId;
    private final Long machineId;
    private final Integer collectInterval;
    private final String apiEndpoint;

    //source는 이벤트를 발행시킨 클래스나 객체를 받음
    public DcpConfigCreatedEvent(Object source, Long dcpConfigId, Long machineId, Integer collectInterval, String apiEndpoint) {
        super(source);
        this.dcpConfigId = dcpConfigId;
        this.machineId = machineId;
        this.collectInterval = collectInterval;
        this.apiEndpoint = apiEndpoint;
    }
}
