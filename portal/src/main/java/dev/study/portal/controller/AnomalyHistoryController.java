package dev.study.portal.controller;

import dev.study.portal.dto.anomalyHistory.AnomalyHistoryResponseDto;
import dev.study.portal.service.AnomalyHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Anomaly History", description = "이상 탐지 이력 관리 API")
@RestController
@RequestMapping("/api/anomaly-histories")
@RequiredArgsConstructor
@Slf4j
public class AnomalyHistoryController {

    private final AnomalyHistoryService anomalyHistoryService;

    @Operation(summary = "모든 이상 탐지 이력 조회", description = "시스템에 기록된 모든 이상 탐지 이력을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AnomalyHistoryResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<AnomalyHistoryResponseDto>> getAll(){
        List<AnomalyHistoryResponseDto> histories = anomalyHistoryService.getAll();
        return ResponseEntity.ok(histories);
    }

    @Operation(summary = "기간별 이상 탐지 이력 조회", description = "지정된 기간 내에 발생한 이상 탐지 이력을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AnomalyHistoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/search")
    public ResponseEntity<List<AnomalyHistoryResponseDto>> getByDetectedAtBetween(
            @Parameter(description = "검색 시작 시간 (ISO-8601 형식: yyyy-MM-dd'T'HH:mm:ss)",
                       example = "2025-01-01T00:00:00", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @Parameter(description = "검색 종료 시간 (ISO-8601 형식: yyyy-MM-dd'T'HH:mm:ss)",
                       example = "2025-12-31T23:59:59", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ){
        List<AnomalyHistoryResponseDto> histories = anomalyHistoryService.findByDetectedAtBetween(start, end);
        return ResponseEntity.ok(histories);
    }
}
