package dev.study.portal.dto.dcp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DcpConfigModifyDto {
    private Long machineId;
    private Integer collectInterval;
    private String apiEndpoint;
}