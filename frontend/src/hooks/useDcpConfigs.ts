import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { dcpConfigApi } from '../api/dcpConfig';
import type { DcpConfigCreateDto, DcpConfigModifyDto } from '../types/api';

export const useDcpConfigs = () => {
  return useQuery({
    queryKey: ['dcpConfigs'],
    queryFn: async () => {
      const { data } = await dcpConfigApi.getAll();
      return data;
    },
  });
};

export const useDcpConfig = (id: number) => {
  return useQuery({
    queryKey: ['dcpConfigs', id],
    queryFn: async () => {
      const { data } = await dcpConfigApi.getById(id);
      return data;
    },
    enabled: !!id,
  });
};

export const useCreateDcpConfig = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: DcpConfigCreateDto) => {
      await dcpConfigApi.create(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dcpConfigs'] });
    },
  });
};

export const useUpdateDcpConfig = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: DcpConfigModifyDto }) => {
      await dcpConfigApi.update(id, data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dcpConfigs'] });
    },
  });
};

export const useDeleteDcpConfig = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      await dcpConfigApi.delete(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dcpConfigs'] });
    },
  });
};
