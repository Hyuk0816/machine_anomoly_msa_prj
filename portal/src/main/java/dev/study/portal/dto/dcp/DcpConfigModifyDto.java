package dev.study.portal.dto.dcp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "DCP 설정 수정 요청 DTO")
@Getter
@Builder
public class DcpConfigModifyDto {

    @Schema(description = "설비 ID", example = "1")
    private Long machineId;

    @Schema(description = "데이터 수집 주기 (초 단위)", example = "120")
    private Integer collectInterval;

    @Schema(description = "데이터 수집 API 엔드포인트", example = "http://simulator:8081/api/sensor/data")
    private String apiEndpoint;
}