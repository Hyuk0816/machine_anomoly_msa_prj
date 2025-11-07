package dev.study.portal.repository.machine;

import dev.study.portal.entity.machine.Machine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineRepository extends JpaRepository<Machine, Long> {
}
