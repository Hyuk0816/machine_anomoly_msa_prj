package dev.study.portal.dto.machine;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "설비 응답 DTO")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MachineResponseDto {

    @Schema(description = "설비 ID", example = "1")
    private Long id;

    @Schema(description = "설비 이름", example = "CNC Machine 1")
    private String name;

    @Schema(description = "설비 유형", example = "CNC")
    private String type;
}
