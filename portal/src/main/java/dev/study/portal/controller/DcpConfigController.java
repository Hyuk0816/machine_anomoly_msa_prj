package dev.study.portal.controller;

import dev.study.portal.dto.dcp.DcpConfigCreateDto;
import dev.study.portal.dto.dcp.DcpConfigModifyDto;
import dev.study.portal.dto.dcp.DcpConfigResponseDto;
import dev.study.portal.service.DcpConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "DCP Config", description = "데이터 수집 계획 설정 관리 API")
@RestController
@RequestMapping("/api/dcp-config")
@RequiredArgsConstructor
public class DcpConfigController {

    private final DcpConfigService dcpConfigService;

    @Operation(summary = "모든 DCP 설정 조회", description = "시스템에 등록된 모든 데이터 수집 계획 설정을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public List<DcpConfigResponseDto> getAll() {
        return dcpConfigService.getAll();
    }

    @Operation(summary = "특정 DCP 설정 조회", description = "ID로 특정 데이터 수집 계획 설정의 상세 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "DCP 설정을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public DcpConfigResponseDto getById(
            @Parameter(description = "DCP 설정 ID", required = true) @PathVariable Long id) {
        return dcpConfigService.getById(id);
    }

    @Operation(summary = "DCP 설정 생성", description = "새로운 데이터 수집 계획 설정을 등록하고 스케줄러를 시작합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "DCP 설정 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody DcpConfigCreateDto dto) {
        dcpConfigService.create(dto);
    }

    @Operation(summary = "DCP 설정 수정", description = "기존 데이터 수집 계획 설정을 수정하고 스케줄러를 재시작합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "DCP 설정을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void modify(
            @Parameter(description = "DCP 설정 ID", required = true) @PathVariable Long id,
            @RequestBody DcpConfigModifyDto dto) {
        dcpConfigService.modify(id, dto);
    }

    @Operation(summary = "DCP 설정 삭제", description = "특정 데이터 수집 계획 설정을 삭제하고 스케줄러를 중지합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "DCP 설정을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "DCP 설정 ID", required = true) @PathVariable Long id) {
        dcpConfigService.delete(id);
    }
}