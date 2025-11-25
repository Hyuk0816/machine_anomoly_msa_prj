import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { machineApi } from '../api/machines';
import type { MachineCreateDto, MachineModifyDto } from '../types/api';

export const useMachines = () => {
  return useQuery({
    queryKey: ['machines'],
    queryFn: async () => {
      const { data } = await machineApi.getAll();
      return data;
    },
  });
};

export const useMachine = (id: number) => {
  return useQuery({
    queryKey: ['machines', id],
    queryFn: async () => {
      const { data } = await machineApi.getById(id);
      return data;
    },
    enabled: !!id,
  });
};

export const useCreateMachine = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: MachineCreateDto) => {
      await machineApi.create(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['machines'] });
    },
  });
};

export const useUpdateMachine = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: MachineModifyDto }) => {
      await machineApi.update(id, data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['machines'] });
    },
  });
};

export const useDeleteMachine = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      await machineApi.delete(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['machines'] });
    },
  });
};
