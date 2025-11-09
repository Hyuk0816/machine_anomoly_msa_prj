package dev.study.portal.event.handler;

import dev.study.portal.event.dcp.DcpConfigCreatedEvent;
import dev.study.portal.event.dcp.DcpConfigDeletedEvent;
import dev.study.portal.event.dcp.DcpConfigModifyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DcpConfigEventHandler {
    private final DynamicSchedulerManager schedulerManager;

    @Async
    @EventListener
    public void handleCreated(DcpConfigCreatedEvent event) {
        log.info("📢 [Event Handler] DcpConfig 생성 이벤트 수신 - ID: {}", event.getDcpConfigId());

        schedulerManager.createScheduler(
                event.getDcpConfigId(),
                event.getMachineId(),
                event.getCollectInterval(),
                event.getApiEndpoint()
        );
    }

    @Async
    @EventListener
    public void handleUpdated(DcpConfigModifyEvent event) {
        log.info("📢 [Event Handler] DcpConfig 수정 이벤트 수신 - ID: {}", event.getDcpConfigId());

        schedulerManager.updateScheduler(
                event.getDcpConfigId(),
                event.getMachineId(),
                event.getCollectInterval(),
                event.getApiEndpoint()
        );
    }

    @Async
    @EventListener
    public void handleDeleted(DcpConfigDeletedEvent event) {
        log.info("📢 [Event Handler] DcpConfig 삭제 이벤트 수신 - ID: {}", event.getDcpConfigId());

        schedulerManager.removeScheduler(event.getDcpConfigId());
    }
}
