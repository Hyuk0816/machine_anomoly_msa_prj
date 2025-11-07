package dev.study.portal.service;

import dev.study.portal.common.exception.dcp.DcpConfigNotFoundException;
import dev.study.portal.common.exception.machine.MachineNotFoundException;
import dev.study.portal.dto.dcp.DcpConfigCreateDto;
import dev.study.portal.dto.dcp.DcpConfigModifyDto;
import dev.study.portal.dto.dcp.DcpConfigResponseDto;
import dev.study.portal.entity.dcp.DcpConfig;
import dev.study.portal.entity.machine.Machine;
import dev.study.portal.repository.dcp.DcpConfigRepository;
import dev.study.portal.repository.machine.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DcpConfigService {

    private final DcpConfigRepository dcpConfigRepository;
    private final MachineRepository machineRepository;
    
    @Transactional(readOnly = true)
    public List<DcpConfigResponseDto> getAll() {
        return dcpConfigRepository.findAll().stream()
                .map(DcpConfig::from)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public DcpConfigResponseDto getById(Long id) {
        DcpConfig dcpConfig = dcpConfigRepository.findById(id)
                .orElseThrow(DcpConfigNotFoundException::new);
        return DcpConfig.from(dcpConfig);
    }

    @Transactional
    public void create(DcpConfigCreateDto dto) {
        
        Machine machine = machineRepository.findById(dto.getMachineId())
                    .orElseThrow(MachineNotFoundException::new);

        DcpConfig dcpConfig = DcpConfig.builder()
                .machine(machine)
                .collectInterval(dto.getCollectInterval())
                .apiEndpoint(dto.getApiEndpoint())
                .build();

        dcpConfigRepository.save(dcpConfig);
    }

    @Transactional
    public void modify(Long id, DcpConfigModifyDto dto) {
        DcpConfig dcpConfig = dcpConfigRepository.findById(id)
                .orElseThrow(DcpConfigNotFoundException::new);
        
        Machine machine = machineRepository.findById(dto.getMachineId())
                    .orElseThrow(MachineNotFoundException::new);
        

        dcpConfig.modify(machine, dto.getCollectInterval(), dto.getApiEndpoint());
    }

    @Transactional
    public void delete(Long id) {
        if (!dcpConfigRepository.existsById(id)) {
            throw new DcpConfigNotFoundException();
        }
        dcpConfigRepository.deleteById(id);
    }
}