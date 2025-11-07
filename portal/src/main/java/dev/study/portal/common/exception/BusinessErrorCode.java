package dev.study.portal.common.exception;

import org.springframework.http.HttpStatus;

public interface BusinessErrorCode {
	String getCode();

	HttpStatus getHttpStatus();

	String getMessage();
}
