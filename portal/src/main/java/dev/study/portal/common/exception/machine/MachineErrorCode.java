package dev.study.portal.common.exception.machine;

import dev.study.portal.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MachineErrorCode implements BusinessErrorCode {
    MACHINE_NOT_FOUND(HttpStatus.NOT_FOUND,
        "ERROR_MACHINE_NOT_FOUND",
        "설비를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
