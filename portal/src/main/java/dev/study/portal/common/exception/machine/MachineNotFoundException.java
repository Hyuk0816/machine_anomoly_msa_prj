package dev.study.portal.common.exception.machine;

import dev.study.portal.common.exception.BusinessException;

public class MachineNotFoundException extends BusinessException {
    public MachineNotFoundException() {
        super(MachineErrorCode.MACHINE_NOT_FOUND);
    }
}
