import { useQuery } from '@tanstack/react-query';
import { anomalyApi } from '../api/anomalies';

export const useAnomalies = () => {
  return useQuery({
    queryKey: ['anomalies'],
    queryFn: async () => {
      const { data } = await anomalyApi.getAll();
      return data;
    },
  });
};

export const useAnomalySearch = (startDate: string, endDate: string) => {
  return useQuery({
    queryKey: ['anomalies', 'search', startDate, endDate],
    queryFn: async () => {
      const { data } = await anomalyApi.search(startDate, endDate);
      return data;
    },
    enabled: !!startDate && !!endDate,
  });
};
