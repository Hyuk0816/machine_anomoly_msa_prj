import { apiClient } from './client';
import type { MachineSensorDataResponseDto } from '../types/api';

export const sensorDataApi = {
  getByMachineIdAndDateRange: (
    machineId: number,
    startDateTime: string,
    endDateTime: string
  ) =>
    apiClient.get<MachineSensorDataResponseDto[]>(
      `/api/machine-sensor-data/${machineId}`,
      {
        params: {
          startDateTime,
          endDateTime,
        },
      }
    ),
};
