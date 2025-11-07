package dev.study.portal.common.exception.dcp;

import dev.study.portal.common.exception.BusinessException;

public class DuplicatedMachineDcpConfigException extends BusinessException {
    public DuplicatedMachineDcpConfigException() {
        super(DcpConfigErrorCode.DUPLICATED_MACHINE_DCP_CONFIG);
    }
}
