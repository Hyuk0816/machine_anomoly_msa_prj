package dev.study.portal.dto.sensor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "센서 데이터 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataDto {

    @Schema(description = "설비 ID", example = "1", required = true)
    private Long machineId;

    @Schema(description = "DCP 설정 ID", example = "1", required = true)
    private Long dcpConfigId;

    @Schema(description = "대기 온도 (K)", example = "298.1", required = true)
    private Double airTemperature;

    @Schema(description = "공정 온도 (K)", example = "308.6", required = true)
    private Double processTemperature;

    @Schema(description = "회전 속도 (rpm)", example = "1551", required = true)
    private Integer rotationalSpeed;

    @Schema(description = "토크 (Nm)", example = "42.8", required = true)
    private Double torque;

    @Schema(description = "공구 마모도 (분)", example = "0", required = true)
    private Integer toolWear;

    @Schema(description = "고장 여부 (0: 정상, 1: 고장)", example = "0", required = true)
    private Integer target;

    @Schema(description = "수집 시각", example = "2025-11-18T10:30:00")
    private LocalDateTime collectedAt;
}