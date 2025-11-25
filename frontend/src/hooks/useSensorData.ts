import { useQuery } from '@tanstack/react-query';
import { sensorDataApi } from '../api/sensorData';

export const useSensorData = (
  machineId: number | null,
  startDateTime: string,
  endDateTime: string
) => {
  return useQuery({
    queryKey: ['sensorData', machineId, startDateTime, endDateTime],
    queryFn: async () => {
      if (!machineId) return [];
      const { data } = await sensorDataApi.getByMachineIdAndDateRange(
        machineId,
        startDateTime,
        endDateTime
      );
      return data;
    },
    enabled: !!machineId && !!startDateTime && !!endDateTime,
  });
};
