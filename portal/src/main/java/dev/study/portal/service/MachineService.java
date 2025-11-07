package dev.study.portal.service;

import dev.study.portal.common.exception.machine.MachineNotFoundException;
import dev.study.portal.dto.machine.MachineCreateDto;
import dev.study.portal.dto.machine.MachineModifyDto;
import dev.study.portal.dto.machine.MachineResponseDto;
import dev.study.portal.entity.machine.Machine;
import dev.study.portal.entity.machine.enums.Type;
import dev.study.portal.repository.machine.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MachineService {
    private final MachineRepository machineRepository;
    
    @Transactional(readOnly = true)
    // 모든 Machine 조회
    public List<MachineResponseDto> getAll() {
        return machineRepository.findAll().stream()
                .map(Machine::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    // ID로 Machine 조회
    public MachineResponseDto getById(Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(MachineNotFoundException::new);
        return Machine.from(machine);
    }

    // Machine 생성
    @Transactional
    public void create(MachineCreateDto dto) {
        Machine machine = Machine.builder()
                .name(dto.getName())
                .type(Type.valueOf(dto.getType()))
                .build();

        machineRepository.save(machine);
    }

    // Machine 수정
    @Transactional
    public void modify(Long id, MachineModifyDto dto) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(MachineNotFoundException::new);

        Type type = dto.getType() != null ? Type.valueOf(dto.getType()) : null;
        machine.modify(dto.getName(), type);
    }

    // Machine 삭제
    @Transactional
    public void delete(Long id) {
        if (!machineRepository.existsById(id)) {
            throw new MachineNotFoundException();
        }
        machineRepository.deleteById(id);
    }
}
