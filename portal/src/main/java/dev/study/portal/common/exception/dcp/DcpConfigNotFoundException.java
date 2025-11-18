package dev.study.portal.common.exception.dcp;

import dev.study.portal.common.exception.BusinessException;

public class DcpConfigNotFoundException extends BusinessException {
    public DcpConfigNotFoundException() {
        super(DcpConfigErrorCode.DCP_CONFIG_NOT_FOUND);
    }
}