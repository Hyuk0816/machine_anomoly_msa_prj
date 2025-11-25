import { apiClient } from './client';
import type {
  DcpConfigCreateDto,
  DcpConfigModifyDto,
  DcpConfigResponseDto,
} from '../types/api';

export const dcpConfigApi = {
  getAll: () => apiClient.get<DcpConfigResponseDto[]>('/api/dcp-config'),

  getById: (id: number) =>
    apiClient.get<DcpConfigResponseDto>(`/api/dcp-config/${id}`),

  create: (data: DcpConfigCreateDto) =>
    apiClient.post<void>('/api/dcp-config', data),

  update: (id: number, data: DcpConfigModifyDto) =>
    apiClient.put<void>(`/api/dcp-config/${id}`, data),

  delete: (id: number) =>
    apiClient.delete<void>(`/api/dcp-config/${id}`),
};
