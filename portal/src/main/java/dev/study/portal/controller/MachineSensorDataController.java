package dev.study.portal.controller;

import dev.study.portal.dto.machine.MachineSensorDataResponseDto;
import dev.study.portal.service.MachineSensorDataService;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Machine Sensor Data", description = "기계 센서 데이터 조회 API")
@RestController
@RequestMapping("/api/machine-sensor-data")
@RequiredArgsConstructor
@Slf4j
public class MachineSensorDataController {
    private final MachineSensorDataService machineSensorDataService;

    @Operation(summary = "기계별 센서 데이터 조회", description = "특정 기계의 지정된 기간 내 센서 데이터를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MachineSensorDataResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "404", description = "기계를 찾을 수 없음")
    })
    @GetMapping("/{machineId}")
    public ResponseEntity<List<MachineSensorDataResponseDto>> getMachineSensorData(
            @Parameter(description = "기계 ID", example = "1", required = true)
            @PathVariable Long machineId,

            @Parameter(description = "검색 시작 시간 (ISO-8601 형식: yyyy-MM-dd'T'HH:mm:ss)",
                       example = "2025-01-01T00:00:00", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDateTime,

            @Parameter(description = "검색 종료 시간 (ISO-8601 형식: yyyy-MM-dd'T'HH:mm:ss)",
                       example = "2025-12-31T23:59:59", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDateTime
    ) {
        List<MachineSensorDataResponseDto> sensorData = machineSensorDataService.getSensorDataCreatedAtBetween(machineId, startDateTime, endDateTime);
        return ResponseEntity.ok(sensorData);
    }
}
