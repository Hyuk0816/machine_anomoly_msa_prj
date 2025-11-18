package dev.study.portal.dto.dcp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "DCP 설정 생성 요청 DTO")
@Getter
@Builder
public class DcpConfigCreateDto {

    @Schema(description = "설비 ID", example = "1", required = true)
    private Long machineId;

    @Schema(description = "데이터 수집 주기 (초 단위)", example = "60", required = true)
    private Integer collectInterval;

    @Schema(description = "데이터 수집 API 엔드포인트", example = "http://simulator:8081/api/sensor/data", required = true)
    private String apiEndpoint;
}