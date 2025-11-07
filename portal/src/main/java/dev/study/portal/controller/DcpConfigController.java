package dev.study.portal.controller;

import dev.study.portal.dto.dcp.DcpConfigCreateDto;
import dev.study.portal.dto.dcp.DcpConfigModifyDto;
import dev.study.portal.dto.dcp.DcpConfigResponseDto;
import dev.study.portal.service.DcpConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dcp-config")
@RequiredArgsConstructor
public class DcpConfigController {

    private final DcpConfigService dcpConfigService;

    @GetMapping
    public List<DcpConfigResponseDto> getAll() {
        return dcpConfigService.getAll();
    }

    @GetMapping("/{id}")
    public DcpConfigResponseDto getById(@PathVariable Long id) {
        return dcpConfigService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody DcpConfigCreateDto dto) {
        dcpConfigService.create(dto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void modify(@PathVariable Long id, @RequestBody DcpConfigModifyDto dto) {
        dcpConfigService.modify(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dcpConfigService.delete(id);
    }
}