package dev.study.portal.dto.machine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MachineResponseDto {
    private Long id;
    private String name;
    private String type;
}
