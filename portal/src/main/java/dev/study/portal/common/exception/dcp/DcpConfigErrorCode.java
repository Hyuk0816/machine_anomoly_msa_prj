package dev.study.portal.common.exception.dcp;

import dev.study.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DcpConfigErrorCode implements BusinessErrorCode {
    DCP_CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND, "DCP_NOT_FOUNT", "DCP 설정을 찾을 수 없습니다."),
    DUPLICATED_MACHINE_DCP_CONFIG(HttpStatus.CONFLICT, "DCP_MACHINE_DUPLICATED", "해당 설비에 대한 DCP 설정이 이미 존재합니다."),
    DUPLICATE_DCP_CONFIG(HttpStatus.CONFLICT, "DCP_DUPLICATED", "중복된 DCP 설정이 존재합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}