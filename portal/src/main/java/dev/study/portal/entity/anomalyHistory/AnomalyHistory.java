package dev.study.portal.entity.anomalyHistory;

import dev.study.portal.entity.machine.Machine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "anomaly_history")
public class AnomalyHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    // 탐지 시각
    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    // 이상 확률 (0.0 ~ 1.0)
    @Column(name = "anomaly_probability", nullable = false)
    private Double anomalyProbability;

    // 센서 데이터 스냅샷 (JSON)
    @Column(name = "sensor_data", columnDefinition = "jsonb", nullable = false)
    private String sensorData;
}
