import { apiClient } from './client';
import type { AnomalyHistoryResponseDto } from '../types/api';

export const anomalyApi = {
  getAll: () =>
    apiClient.get<AnomalyHistoryResponseDto[]>('/api/anomaly-histories'),

  search: (startDate: string, endDate: string) =>
    apiClient.get<AnomalyHistoryResponseDto[]>('/api/anomaly-histories/search', {
      params: {
        start: startDate,
        end: endDate,
      },
    }),
};
