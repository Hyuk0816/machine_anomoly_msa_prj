package dev.study.portal.controller;

import dev.study.portal.dto.machine.MachineCreateDto;
import dev.study.portal.dto.machine.MachineModifyDto;
import dev.study.portal.dto.machine.MachineResponseDto;
import dev.study.portal.service.MachineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machine")
@RequiredArgsConstructor
public class MachineController {
    private final MachineService machineService;

    // 모든 설비 조회
    @GetMapping
    public ResponseEntity<List<MachineResponseDto>> getAllMachines() {
        List<MachineResponseDto> machines = machineService.getAll();
        return ResponseEntity.ok(machines);
    }

    // 특정 설비 조회
    @GetMapping("/{id}")
    public ResponseEntity<MachineResponseDto> getMachineById(@PathVariable Long id) {
        MachineResponseDto machine = machineService.getById(id);
        return ResponseEntity.ok(machine);
    }

    // 설비 생성
    @PostMapping
    public ResponseEntity<Void> createMachine(@RequestBody MachineCreateDto dto) {
        machineService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 설비 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> modifyMachine(@PathVariable Long id, @RequestBody MachineModifyDto dto) {
        machineService.modify(id, dto);
        return ResponseEntity.ok().build();
    }

    // 설비 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long id) {
        machineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}