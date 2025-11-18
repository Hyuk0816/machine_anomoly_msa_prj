package dev.study.portal.service;

import dev.study.portal.common.exception.dcp.DcpConfigNotFoundException;
import dev.study.portal.common.exception.machine.MachineNotFoundException;
import dev.study.portal.dto.dcp.DcpConfigCreateDto;
import dev.study.portal.dto.dcp.DcpConfigModifyDto;
import dev.study.portal.dto.dcp.DcpConfigResponseDto;
import dev.study.portal.entity.dcp.DcpConfig;
import dev.study.portal.entity.machine.Machine;
import dev.study.portal.entity.machine.enums.Type;
import dev.study.portal.repository.dcp.DcpConfigRepository;
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
class DcpConfigServiceTest {

    @Mock
    private DcpConfigRepository dcpConfigRepository;

    @Mock
    private MachineRepository machineRepository;

    @InjectMocks
    private DcpConfigService dcpConfigService;

    private DcpConfig testDcpConfig;
    private DcpConfig testDcpConfig2;
    private Machine testMachine;

    @BeforeEach
    void setUp() {
        // Machine 설정
        testMachine = Machine.builder()
                .name("Test Machine")
                .type(Type.HIGH)
                .build();
        setId(testMachine, 1L);

        // DcpConfig 설정
        testDcpConfig = DcpConfig.builder()
                .machine(testMachine)
                .collectInterval(60)
                .apiEndpoint("http://api.example.com/data")
                .build();
        setId(testDcpConfig, 1L);

        testDcpConfig2 = DcpConfig.builder()
                .machine(testMachine)
                .collectInterval(120)
                .apiEndpoint("http://api.example.com/data2")
                .build();
        setId(testDcpConfig2, 2L);
    }

    // Helper method to set ID using reflection
    private void setId(Object entity, Long id) {
        try {
            var idField = entity.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }

    @Test
    @DisplayName("모든 DCP 설정 조회시 설정 목록을 반환한다")
    void getAll_ReturnsListOfDcpConfigs() {
        // Given
        List<DcpConfig> configs = Arrays.asList(testDcpConfig, testDcpConfig2);
        given(dcpConfigRepository.findAll()).willReturn(configs);

        // When
        List<DcpConfigResponseDto> result = dcpConfigService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getMachineId()).isEqualTo(1L);
        assertThat(result.get(0).getCollectInterval()).isEqualTo(60);
        assertThat(result.get(0).getApiEndpoint()).isEqualTo("http://api.example.com/data");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getCollectInterval()).isEqualTo(120);
        then(dcpConfigRepository).should(times(1)).findAll();
    }

    @Test
    @DisplayName("빈 리스트 조회시 빈 리스트를 반환한다")
    void getAll_WhenNoDcpConfigs_ReturnsEmptyList() {
        // Given
        given(dcpConfigRepository.findAll()).willReturn(Arrays.asList());

        // When
        List<DcpConfigResponseDto> result = dcpConfigService.getAll();

        // Then
        assertThat(result).isEmpty();
        then(dcpConfigRepository).should(times(1)).findAll();
    }

    @Test
    @DisplayName("존재하는 ID로 DCP 설정 조회시 설정 정보를 반환한다")
    void getById_WhenDcpConfigExists_ReturnsDcpConfig() {
        // Given
        Long configId = 1L;
        given(dcpConfigRepository.findById(configId)).willReturn(Optional.of(testDcpConfig));

        // When
        DcpConfigResponseDto result = dcpConfigService.getById(configId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMachineId()).isEqualTo(1L);
        assertThat(result.getCollectInterval()).isEqualTo(60);
        assertThat(result.getApiEndpoint()).isEqualTo("http://api.example.com/data");
        then(dcpConfigRepository).should(times(1)).findById(configId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 DCP 설정 조회시 예외를 발생시킨다")
    void getById_WhenDcpConfigNotExists_ThrowsException() {
        // Given
        Long configId = 999L;
        given(dcpConfigRepository.findById(configId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dcpConfigService.getById(configId))
                .isInstanceOf(DcpConfigNotFoundException.class);
        then(dcpConfigRepository).should(times(1)).findById(configId);
    }

    @Test
    @DisplayName("유효한 Machine ID로 DCP 설정 생성시 성공적으로 저장한다")
    void create_WithValidMachineId_SavesDcpConfig() {
        // Given
        DcpConfigCreateDto createDto = DcpConfigCreateDto.builder()
                .machineId(1L)
                .collectInterval(60)
                .apiEndpoint("http://api.example.com/new")
                .build();

        given(machineRepository.findById(1L)).willReturn(Optional.of(testMachine));

        DcpConfig savedConfig = DcpConfig.builder()
                .machine(testMachine)
                .collectInterval(60)
                .apiEndpoint("http://api.example.com/new")
                .build();
        setId(savedConfig, 3L);

        given(dcpConfigRepository.save(any(DcpConfig.class))).willReturn(savedConfig);

        // When
        dcpConfigService.create(createDto);

        // Then
        then(machineRepository).should(times(1)).findById(1L);
        then(dcpConfigRepository).should(times(1)).save(argThat(config ->
                config.getMachine().equals(testMachine) &&
                config.getCollectInterval() == 60 &&
                config.getApiEndpoint().equals("http://api.example.com/new")
        ));
    }

    @Test
    @DisplayName("Machine ID가 null일 때 DCP 설정 생성시 예외를 발생시킨다")
    void create_WithNullMachineId_ThrowsException() {
        // Given
        DcpConfigCreateDto createDto = DcpConfigCreateDto.builder()
                .machineId(null)
                .collectInterval(90)
                .apiEndpoint("http://api.example.com/standalone")
                .build();

        given(machineRepository.findById(null)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dcpConfigService.create(createDto))
                .isInstanceOf(MachineNotFoundException.class);
        then(machineRepository).should(times(1)).findById(null);
        then(dcpConfigRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 Machine ID로 DCP 설정 생성시 예외를 발생시킨다")
    void create_WithInvalidMachineId_ThrowsException() {
        // Given
        DcpConfigCreateDto createDto = DcpConfigCreateDto.builder()
                .machineId(999L)
                .collectInterval(60)
                .apiEndpoint("http://api.example.com/new")
                .build();

        given(machineRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dcpConfigService.create(createDto))
                .isInstanceOf(MachineNotFoundException.class);
        then(machineRepository).should(times(1)).findById(999L);
        then(dcpConfigRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("존재하는 DCP 설정 수정시 성공적으로 수정한다")
    void modify_WhenDcpConfigExists_ModifiesDcpConfig() {
        // Given
        Long configId = 1L;
        Machine newMachine = Machine.builder()
                .name("New Machine")
                .type(Type.MEDIUM)
                .build();
        setId(newMachine, 2L);

        DcpConfigModifyDto modifyDto = DcpConfigModifyDto.builder()
                .machineId(2L)
                .collectInterval(180)
                .apiEndpoint("http://api.example.com/modified")
                .build();

        given(dcpConfigRepository.findById(configId)).willReturn(Optional.of(testDcpConfig));
        given(machineRepository.findById(2L)).willReturn(Optional.of(newMachine));

        // When
        dcpConfigService.modify(configId, modifyDto);

        // Then
        then(dcpConfigRepository).should(times(1)).findById(configId);
        then(machineRepository).should(times(1)).findById(2L);
        assertThat(testDcpConfig.getMachine()).isEqualTo(newMachine);
        assertThat(testDcpConfig.getCollectInterval()).isEqualTo(180);
        assertThat(testDcpConfig.getApiEndpoint()).isEqualTo("http://api.example.com/modified");
    }

    @Test
    @DisplayName("Machine ID가 null일 때 DCP 설정 수정시 예외를 발생시킨다")
    void modify_WithNullMachineId_ThrowsException() {
        // Given
        Long configId = 1L;
        DcpConfigModifyDto modifyDto = DcpConfigModifyDto.builder()
                .machineId(null)
                .collectInterval(240)
                .apiEndpoint("http://api.example.com/modified")
                .build();

        given(dcpConfigRepository.findById(configId)).willReturn(Optional.of(testDcpConfig));
        given(machineRepository.findById(null)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dcpConfigService.modify(configId, modifyDto))
                .isInstanceOf(MachineNotFoundException.class);
        then(dcpConfigRepository).should(times(1)).findById(configId);
        then(machineRepository).should(times(1)).findById(null);
    }

    @Test
    @DisplayName("존재하지 않는 DCP 설정 수정시 예외를 발생시킨다")
    void modify_WhenDcpConfigNotExists_ThrowsException() {
        // Given
        Long configId = 999L;
        DcpConfigModifyDto modifyDto = DcpConfigModifyDto.builder()
                .collectInterval(180)
                .apiEndpoint("http://api.example.com/modified")
                .build();

        given(dcpConfigRepository.findById(configId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dcpConfigService.modify(configId, modifyDto))
                .isInstanceOf(DcpConfigNotFoundException.class);
        then(dcpConfigRepository).should(times(1)).findById(configId);
    }

    @Test
    @DisplayName("존재하지 않는 Machine ID로 수정시 예외를 발생시킨다")
    void modify_WithInvalidMachineId_ThrowsException() {
        // Given
        Long configId = 1L;
        DcpConfigModifyDto modifyDto = DcpConfigModifyDto.builder()
                .machineId(999L)
                .build();

        given(dcpConfigRepository.findById(configId)).willReturn(Optional.of(testDcpConfig));
        given(machineRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dcpConfigService.modify(configId, modifyDto))
                .isInstanceOf(MachineNotFoundException.class);
        then(dcpConfigRepository).should(times(1)).findById(configId);
        then(machineRepository).should(times(1)).findById(999L);
    }

    @Test
    @DisplayName("존재하는 DCP 설정 삭제시 성공적으로 삭제한다")
    void delete_WhenDcpConfigExists_DeletesDcpConfig() {
        // Given
        Long configId = 1L;
        given(dcpConfigRepository.existsById(configId)).willReturn(true);

        // When
        dcpConfigService.delete(configId);

        // Then
        then(dcpConfigRepository).should(times(1)).existsById(configId);
        then(dcpConfigRepository).should(times(1)).deleteById(configId);
    }

    @Test
    @DisplayName("존재하지 않는 DCP 설정 삭제시 예외를 발생시킨다")
    void delete_WhenDcpConfigNotExists_ThrowsException() {
        // Given
        Long configId = 999L;
        given(dcpConfigRepository.existsById(configId)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> dcpConfigService.delete(configId))
                .isInstanceOf(DcpConfigNotFoundException.class);
        then(dcpConfigRepository).should(times(1)).existsById(configId);
        then(dcpConfigRepository).should(never()).deleteById(any());
    }
}