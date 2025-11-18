package dev.study.portal.repository.sensor;

import dev.study.portal.entity.machine.MachineSensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineSensorDataRepository extends JpaRepository<MachineSensorData, Long> {

    /**
     * 특정 설비의 센서 데이터 조회
     */
    List<MachineSensorData> findByMachineIdOrderByCreatedAtDesc(Long machineId);

    /**
     * 특정 설비의 최근 N개 데이터 조회
     */
    List<MachineSensorData> findTop10ByMachineIdOrderByCreatedAtDesc(Long machineId);
}