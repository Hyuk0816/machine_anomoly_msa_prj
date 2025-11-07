package dev.study.portal.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class BusinessException extends RuntimeException {
	private final Map<String, Object> extraData = new HashMap<>();
	
	private final BusinessErrorCode errorCode;

	protected BusinessException(BusinessErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	protected BusinessException(
		BusinessErrorCode errorCode,
		String message
	) {
		super(message);
		this.errorCode = errorCode;
	}

	protected BusinessException(
		BusinessErrorCode errorCode,
		Throwable cause
	) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}

	protected BusinessException(
		BusinessErrorCode errorCode,
		String message,
		Throwable cause
	) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	protected final void addExtraData(
		String key,
		Object value
	) {
		extraData.put(key, value);
	}
}
