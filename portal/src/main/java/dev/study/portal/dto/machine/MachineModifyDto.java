package dev.study.portal.dto.machine;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "설비 수정 요청 DTO")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MachineModifyDto {

    @Schema(description = "설비 이름", example = "Updated CNC Machine 1")
    private String name;

    @Schema(description = "설비 유형", example = "CNC")
    private String type;
}
