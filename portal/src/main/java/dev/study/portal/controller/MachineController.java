package dev.study.portal.controller;

import dev.study.portal.dto.machine.MachineCreateDto;
import dev.study.portal.dto.machine.MachineModifyDto;
import dev.study.portal.dto.machine.MachineResponseDto;
import dev.study.portal.service.MachineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Machine", description = "설비 관리 API")
@RestController
@RequestMapping("/api/machine")
@RequiredArgsConstructor
public class MachineController {
    private final MachineService machineService;

    @Operation(summary = "모든 설비 조회", description = "시스템에 등록된 모든 설비 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<MachineResponseDto>> getAllMachines() {
        List<MachineResponseDto> machines = machineService.getAll();
        return ResponseEntity.ok(machines);
    }

    @Operation(summary = "특정 설비 조회", description = "ID로 특정 설비의 상세 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "설비를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MachineResponseDto> getMachineById(
            @Parameter(description = "설비 ID", required = true) @PathVariable Long id) {
        MachineResponseDto machine = machineService.getById(id);
        return ResponseEntity.ok(machine);
    }

    @Operation(summary = "설비 생성", description = "새로운 설비를 등록합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "설비 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping
    public ResponseEntity<Void> createMachine(@RequestBody MachineCreateDto dto) {
        machineService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "설비 수정", description = "기존 설비 정보를 수정합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "설비를 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> modifyMachine(
            @Parameter(description = "설비 ID", required = true) @PathVariable Long id,
            @RequestBody MachineModifyDto dto) {
        machineService.modify(id, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "설비 삭제", description = "특정 설비를 시스템에서 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "설비를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMachine(
            @Parameter(description = "설비 ID", required = true) @PathVariable Long id) {
        machineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}