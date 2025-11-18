package dev.study.portal.dto.dcp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DcpConfigResponseDto {
    private Long id;
    private Long machineId;
    private Integer collectInterval;
    private String apiEndpoint;
}