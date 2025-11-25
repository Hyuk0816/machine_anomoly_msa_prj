import { apiClient } from './client';
import type {
  MachineCreateDto,
  MachineModifyDto,
  MachineResponseDto,
} from '../types/api';

export const machineApi = {
  getAll: () => apiClient.get<MachineResponseDto[]>('/api/machine'),

  getById: (id: number) =>
    apiClient.get<MachineResponseDto>(`/api/machine/${id}`),

  create: (data: MachineCreateDto) =>
    apiClient.post<void>('/api/machine', data),

  update: (id: number, data: MachineModifyDto) =>
    apiClient.put<void>(`/api/machine/${id}`, data),

  delete: (id: number) =>
    apiClient.delete<void>(`/api/machine/${id}`),
};
