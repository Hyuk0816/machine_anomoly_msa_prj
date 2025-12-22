import { aiClient } from './aiClient';
import type { AnomalyHistoryResponseDto } from '../types/api';

export const anomalyApi = {
  getAll: () =>
    aiClient.get<AnomalyHistoryResponseDto[]>('/anomaly-histories'),

  search: (startDate: string, endDate: string) =>
    aiClient.get<AnomalyHistoryResponseDto[]>('/anomaly-histories/search', {
      params: {
        start: startDate,
        end: endDate,
      },
    }),
};
