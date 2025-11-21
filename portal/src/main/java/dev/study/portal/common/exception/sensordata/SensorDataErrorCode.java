package dev.study.portal.common.exception.sensordata;

import dev.study.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum SensorDataErrorCode implements BusinessErrorCode {

    SENSOR_DATA_JSON_WRITE_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "ERROR_JSON_WRITE_ERRE", "Json 문자열로 변환하지 못했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
