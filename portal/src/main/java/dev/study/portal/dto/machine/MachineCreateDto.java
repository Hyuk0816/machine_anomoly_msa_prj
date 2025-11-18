package dev.study.portal.dto.machine;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "설비 생성 요청 DTO")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MachineCreateDto {

    @Schema(description = "설비 이름", example = "CNC Machine 1", required = true)
    private String name;

    @Schema(description = "설비 유형", example = "CNC", required = true)
    private String type;
}
