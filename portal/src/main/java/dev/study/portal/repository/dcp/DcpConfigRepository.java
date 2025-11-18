package dev.study.portal.repository.dcp;

import dev.study.portal.entity.dcp.DcpConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DcpConfigRepository extends JpaRepository<DcpConfig, Long> {
    Boolean existsByMachineId(Long machineId);
}