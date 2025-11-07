package dev.study.portal.service;

import dev.study.portal.common.exception.machine.MachineNotFoundException;
import dev.study.portal.dto.machine.MachineCreateDto;
import dev.study.portal.dto.machine.MachineModifyDto;
import dev.study.portal.dto.machine.MachineResponseDto;
import dev.study.portal.entity.machine.Machine;
import dev.study.portal.entity.machine.enums.Type;
import dev.study.portal.repository.machine.MachineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MachineServiceTest {

    @Mock
    private MachineRepository machineRepository;

    @InjectMocks
    private MachineService machineService;

    private Machine testMachine;
    private Machine testMachine2;

    @BeforeEach
    void setUp() {
        testMachine = Machine.builder()
                .name("Test Machine 1")
                .type(Type.HIGH)
                .build();
        // Reflection을 사용하여 ID 설정 (BaseEntity의 protected 필드)
        setId(testMachine, 1L);

        testMachine2 = Machine.builder()
                .name("Test Machine 2")
                .type(Type.MEDIUM)
                .build();
        setId(testMachine2, 2L);
    }

    // Helper method to set ID using reflection
    private void setId(Machine machine, Long id) {
        try {
            var idField = machine.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(machine, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }

    @Test
    @DisplayName("모든 설비 조회시 설비 목록을 반환한다")
    void getAll_ReturnsListOfMachines() {
        // Given
        List<Machine> machines = Arrays.asList(testMachine, testMachine2);
        given(machineRepository.findAll()).willReturn(machines);

        // When
        List<MachineResponseDto> result = machineService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Test Machine 1");
        assertThat(result.get(0).getType()).isEqualTo("HIGH");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Test Machine 2");
        assertThat(result.get(1).getType()).isEqualTo("MEDIUM");
        then(machineRepository).should(times(1)).findAll();
    }

    @Test
    @DisplayName("빈 리스트 조회시 빈 리스트를 반환한다")
    void getAll_WhenNoMachines_ReturnsEmptyList() {
        // Given
        given(machineRepository.findAll()).willReturn(Arrays.asList());

        // When
        List<MachineResponseDto> result = machineService.getAll();

        // Then
        assertThat(result).isEmpty();
        then(machineRepository).should(times(1)).findAll();
    }

    @Test
    @DisplayName("존재하는 ID로 설비 조회시 설비 정보를 반환한다")
    void getById_WhenMachineExists_ReturnsMachine() {
        // Given
        Long machineId = 1L;
        given(machineRepository.findById(machineId)).willReturn(Optional.of(testMachine));

        // When
        MachineResponseDto result = machineService.getById(machineId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Machine 1");
        assertThat(result.getType()).isEqualTo("HIGH");
        then(machineRepository).should(times(1)).findById(machineId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 설비 조회시 예외를 발생시킨다")
    void getById_WhenMachineNotExists_ThrowsException() {
        // Given
        Long machineId = 999L;
        given(machineRepository.findById(machineId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> machineService.getById(machineId))
                .isInstanceOf(MachineNotFoundException.class);
        then(machineRepository).should(times(1)).findById(machineId);
    }

    @Test
    @DisplayName("설비 생성시 성공적으로 저장한다")
    void create_ValidInput_SavesMachine() {
        // Given
        MachineCreateDto createDto = MachineCreateDto.builder()
                .name("New Machine")
                .type("LOW")
                .build();

        Machine savedMachine = Machine.builder()
                .name("New Machine")
                .type(Type.LOW)
                .build();
        setId(savedMachine, 3L);

        given(machineRepository.save(any(Machine.class))).willReturn(savedMachine);

        // When
        machineService.create(createDto);

        // Then
        then(machineRepository).should(times(1)).save(argThat(machine ->
                machine.getName().equals("New Machine") &&
                machine.getType() == Type.LOW
        ));
    }

    @Test
    @DisplayName("잘못된 Type으로 설비 생성시 예외를 발생시킨다")
    void create_InvalidType_ThrowsException() {
        // Given
        MachineCreateDto createDto = MachineCreateDto.builder()
                .name("New Machine")
                .type("INVALID_TYPE")
                .build();

        // When & Then
        assertThatThrownBy(() -> machineService.create(createDto))
                .isInstanceOf(IllegalArgumentException.class);
        then(machineRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("존재하는 설비 수정시 성공적으로 수정한다")
    void modify_WhenMachineExists_ModifiesMachine() {
        // Given
        Long machineId = 1L;
        MachineModifyDto modifyDto = MachineModifyDto.builder()
                .name("Modified Machine")
                .type("LOW")
                .build();

        given(machineRepository.findById(machineId)).willReturn(Optional.of(testMachine));

        // When
        machineService.modify(machineId, modifyDto);

        // Then
        then(machineRepository).should(times(1)).findById(machineId);
        assertThat(testMachine.getName()).isEqualTo("Modified Machine");
        assertThat(testMachine.getType()).isEqualTo(Type.LOW);
    }

    @Test
    @DisplayName("일부 필드만 수정시 해당 필드만 업데이트한다")
    void modify_PartialUpdate_OnlyUpdatesProvidedFields() {
        // Given
        Long machineId = 1L;
        MachineModifyDto modifyDto = MachineModifyDto.builder()
                .name("Modified Name Only")
                .type(null)  // type은 수정하지 않음
                .build();

        given(machineRepository.findById(machineId)).willReturn(Optional.of(testMachine));

        // When
        machineService.modify(machineId, modifyDto);

        // Then
        then(machineRepository).should(times(1)).findById(machineId);
        assertThat(testMachine.getName()).isEqualTo("Modified Name Only");
        assertThat(testMachine.getType()).isEqualTo(Type.HIGH);  // 기존 값 유지
    }

    @Test
    @DisplayName("존재하지 않는 설비 수정시 예외를 발생시킨다")
    void modify_WhenMachineNotExists_ThrowsException() {
        // Given
        Long machineId = 999L;
        MachineModifyDto modifyDto = MachineModifyDto.builder()
                .name("Modified Machine")
                .type("LOW")
                .build();

        given(machineRepository.findById(machineId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> machineService.modify(machineId, modifyDto))
                .isInstanceOf(MachineNotFoundException.class);
        then(machineRepository).should(times(1)).findById(machineId);
    }

    @Test
    @DisplayName("존재하는 설비 삭제시 성공적으로 삭제한다")
    void delete_WhenMachineExists_DeletesMachine() {
        // Given
        Long machineId = 1L;
        given(machineRepository.existsById(machineId)).willReturn(true);

        // When
        machineService.delete(machineId);

        // Then
        then(machineRepository).should(times(1)).existsById(machineId);
        then(machineRepository).should(times(1)).deleteById(machineId);
    }

    @Test
    @DisplayName("존재하지 않는 설비 삭제시 예외를 발생시킨다")
    void delete_WhenMachineNotExists_ThrowsException() {
        // Given
        Long machineId = 999L;
        given(machineRepository.existsById(machineId)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> machineService.delete(machineId))
                .isInstanceOf(MachineNotFoundException.class);
        then(machineRepository).should(times(1)).existsById(machineId);
        then(machineRepository).should(never()).deleteById(any());
    }
}