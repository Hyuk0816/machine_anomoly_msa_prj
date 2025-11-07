package dev.study.portal.entity.machine;

import dev.study.portal.entity.BaseEntity;
import dev.study.portal.entity.machine.enums.Type;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "machine")
@Comment("설비의 실시간 센서 데이터를 수집하여 영속화 하는 테이블")
@SuperBuilder
public class MachineSensorData extends BaseEntity {

    @Column(name = "machine_id")
    private Long machineId;

    @Column(name = "air_temperature")
    private Double airTemperature;

    @Column(name = "process_temperature")
    private Double processTemperature;

    @Column(name = "rotational_speed")
    @Comment("회전 속도 (RPM)")
    private Integer rotationalSpeed;

    @Column(name = "torque")
    private Double torque;

    @Column(name = "tool_wear")
    private Integer toolWear;

    @Column(name = "target")
    private Integer target;
}
