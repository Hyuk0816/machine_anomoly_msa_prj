package dev.study.portal.listener.dcp;

import dev.study.portal.entity.dcp.DcpConfig;
import dev.study.portal.event.dcp.DcpConfigCreatedEvent;
import dev.study.portal.event.dcp.DcpConfigDeletedEvent;
import dev.study.portal.event.dcp.DcpConfigModifyEvent;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class DcpConfigEntityEventListener {
    private final ApplicationEventPublisher applicationEventPublisher;

    public DcpConfigEntityEventListener(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    //dcp config 생성 감지
    @PostPersist
    public void onPostPersist(DcpConfig dcpConfig) {
        log.info("DcpConfig 생성 감지 - ID: {}, Machine: {}, Interval: {}초",
                dcpConfig.getId(),
                dcpConfig.getMachine() != null ? dcpConfig.getMachine().getId() : null,
                dcpConfig.getCollectInterval());

        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new DcpConfigCreatedEvent(
                    this,
                    dcpConfig.getId(),
                    dcpConfig.getMachine() != null ? dcpConfig.getMachine().getId() : null,
                    dcpConfig.getCollectInterval(),
                    dcpConfig.getApiEndpoint()
            ));
        }
    }

    @PostUpdate
    public void onPostUpdate(DcpConfig dcpConfig) {
        log.info("DcpConfig 수정 감지 - ID: {}, Machine: {}, Interval: {}초",
                dcpConfig.getId(),
                dcpConfig.getMachine() != null ? dcpConfig.getMachine().getId() : null,
                dcpConfig.getCollectInterval());

        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new DcpConfigModifyEvent(
                    this,
                    dcpConfig.getId(),
                    dcpConfig.getMachine() != null ? dcpConfig.getMachine().getId() : null,
                    dcpConfig.getCollectInterval(),
                    dcpConfig.getApiEndpoint()
            ));
        }
    }

    @PostRemove
    public void onPostRemove(DcpConfig dcpConfig) {
        log.info("DcpConfig 삭제 감지 - ID: {}", dcpConfig.getId());

        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new DcpConfigDeletedEvent(
                    this,
                    dcpConfig.getId()
            ));
        }
    }
}
