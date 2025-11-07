package dev.study.portal.entity.machine;

import dev.study.portal.dto.machine.MachineResponseDto;
import dev.study.portal.entity.BaseEntity;
import dev.study.portal.entity.machine.enums.Type;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
public class Machine extends BaseEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    public static MachineResponseDto from(Machine machine) {
        return MachineResponseDto.builder()
                .id(machine.getId())
                .name(machine.getName())
                .type(machine.getType().name())
                .build();
    }

    public void modify(String name, Type type) {
        if (name != null) {
            this.name = name;
        }
        if (type != null) {
            this.type = type;
        }
    }
}
