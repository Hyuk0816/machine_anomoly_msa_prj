package dev.study.portal.entity.dcp;

import dev.study.portal.dto.dcp.DcpConfigResponseDto;
import dev.study.portal.entity.BaseEntity;
import dev.study.portal.entity.machine.Machine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "dcp_config")
public class DcpConfig extends BaseEntity {
    @JoinColumn(name = "machine_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Machine machine;

    @Column(name = "collect_interval")
    @Comment("데이터 수집 주기 (초)")
    private Integer collectInterval;

    @Column(name = "api_endpoint")
    @Comment("DCP API 엔드포인트")
    private String apiEndpoint;

    public static DcpConfigResponseDto from(DcpConfig dcpConfig) {
        return DcpConfigResponseDto.builder()
                .id(dcpConfig.getId())
                .machineId(dcpConfig.getMachine() != null ? dcpConfig.getMachine().getId() : null)
                .collectInterval(dcpConfig.getCollectInterval())
                .apiEndpoint(dcpConfig.getApiEndpoint())
                .build();
    }

    public void modify(Machine machine, Integer collectInterval, String apiEndpoint) {
        if (machine != null) {
            this.machine = machine;
        }
        if (collectInterval != null) {
            this.collectInterval = collectInterval;
        }
        if (apiEndpoint != null) {
            this.apiEndpoint = apiEndpoint;
        }
    }
}
