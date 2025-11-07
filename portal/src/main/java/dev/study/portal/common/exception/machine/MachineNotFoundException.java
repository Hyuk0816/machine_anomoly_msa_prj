package dev.study.portal.common.exception.machine;

import dev.study.portal.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;

public class MachineNotFoundException extends BusinessException {
    public MachineNotFoundException() {
        super(MachineErrorCode.MACHINE_NOT_FOUND);
    }
}
