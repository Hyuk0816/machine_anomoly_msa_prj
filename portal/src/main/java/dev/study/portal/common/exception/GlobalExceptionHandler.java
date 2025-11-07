package dev.study.portal.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("Business exception occurred: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getMessage())
                .extraData(e.getExtraData())
                .build();

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("ERROR_INTERNAL_SERVER")
                .message("서버 내부 오류가 발생했습니다.")
                .build();

        return ResponseEntity
                .status(500)
                .body(errorResponse);
    }
}