package dev.study.portal.common.exception.sensordata;

import dev.study.portal.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SensorDataJsonWriteException extends BusinessException {
    public SensorDataJsonWriteException(Exception e) {
        super(SensorDataErrorCode.SENSOR_DATA_JSON_WRITE_ERROR);
        log.error(e.getMessage(), e);
    }
}
