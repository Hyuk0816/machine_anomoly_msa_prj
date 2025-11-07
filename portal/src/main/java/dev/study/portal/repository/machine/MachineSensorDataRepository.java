package dev.study.portal.repository.machine;

import dev.study.portal.entity.machine.MachineSensorData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineSensorDataRepository extends JpaRepository<MachineSensorData, Long> {
}
