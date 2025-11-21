package dev.study.portal.repository.sensor;

import dev.study.portal.entity.machine.MachineSensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MachineSensorDataRepository extends JpaRepository<MachineSensorData, Long> {

    List<MachineSensorData> findByMachineIdAndCreatedAtBetween(Long machineId, LocalDateTime startAt, LocalDateTime endAt);
}