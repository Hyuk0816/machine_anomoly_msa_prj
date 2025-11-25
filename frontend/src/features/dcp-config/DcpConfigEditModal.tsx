import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../components/ui/select';
import { useUpdateDcpConfig } from '../../hooks/useDcpConfigs';
import { useMachines } from '../../hooks/useMachines';
import { useToast } from '../../hooks/useToast';
import type { DcpConfigResponseDto } from '../../types/api';

const dcpConfigSchema = z.object({
  machineId: z.number().min(1, '설비 선택은 필수입니다'),
  collectInterval: z.number().min(1, '수집 주기는 최소 1초 이상이어야 합니다'),
  apiEndpoint: z.string().url('유효하지 않은 URL입니다'),
});

type DcpConfigFormData = z.infer<typeof dcpConfigSchema>;

interface DcpConfigEditModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  config: DcpConfigResponseDto | null;
}

export function DcpConfigEditModal({
  open,
  onOpenChange,
  config,
}: DcpConfigEditModalProps) {
  const { toast } = useToast();
  const updateConfig = useUpdateDcpConfig();
  const { data: machines } = useMachines();
  const [selectedMachineId, setSelectedMachineId] = useState<string>('');

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    setValue,
  } = useForm<DcpConfigFormData>({
    resolver: zodResolver(dcpConfigSchema),
  });

  useEffect(() => {
    if (config) {
      setValue('machineId', config.machineId);
      setValue('collectInterval', config.collectInterval);
      setValue('apiEndpoint', config.apiEndpoint);
      setSelectedMachineId(config.machineId.toString());
    }
  }, [config, setValue]);

  const onSubmit = async (data: DcpConfigFormData) => {
    if (!config) return;

    try {
      await updateConfig.mutateAsync({
        id: config.id,
        data: {
          machineId: data.machineId,
          collectInterval: data.collectInterval,
          apiEndpoint: data.apiEndpoint,
        },
      });
      toast({
        title: '성공',
        description: 'DCP 설정이 수정되었습니다',
      });
      reset();
      setSelectedMachineId('');
      onOpenChange(false);
    } catch (error) {
      toast({
        title: '오류',
        description: 'DCP 설정 수정에 실패했습니다',
        variant: 'destructive',
      });
    }
  };

  const handleClose = () => {
    reset();
    setSelectedMachineId('');
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="bg-slate-900 border border-slate-700/50 rounded-xl shadow-2xl">
        <DialogHeader className="border-b border-slate-700/50 pb-4">
          <DialogTitle className="text-white text-xl">DCP 설정 수정</DialogTitle>
          <DialogDescription className="text-slate-400">
            데이터 수집 파라미터를 수정합니다
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-4">
          <div className="space-y-2">
            <Label htmlFor="edit-machineId" className="text-slate-300">설비</Label>
            <Select
              value={selectedMachineId}
              onValueChange={(value) => {
                setSelectedMachineId(value);
                setValue('machineId', parseInt(value));
              }}
            >
              <SelectTrigger className="bg-slate-900/50 border border-slate-700/50 text-white rounded-lg">
                <SelectValue placeholder="설비를 선택하세요" />
              </SelectTrigger>
              <SelectContent className="bg-slate-900 border border-slate-700/50">
                {machines?.map((machine) => (
                  <SelectItem
                    key={machine.id}
                    value={machine.id.toString()}
                    className="text-white hover:bg-slate-800 focus:bg-slate-800"
                  >
                    {machine.name} ({machine.type})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.machineId && (
              <p className="text-sm text-red-400">{errors.machineId.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="edit-collectInterval" className="text-slate-300">수집 주기 (초)</Label>
            <Input
              id="edit-collectInterval"
              type="number"
              placeholder="60"
              className="bg-slate-900/50 border border-slate-700/50 text-white rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent placeholder:text-slate-500"
              {...register('collectInterval', { valueAsNumber: true })}
            />
            {errors.collectInterval && (
              <p className="text-sm text-red-400">
                {errors.collectInterval.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="edit-apiEndpoint" className="text-slate-300">API 엔드포인트</Label>
            <Input
              id="edit-apiEndpoint"
              type="url"
              placeholder="http://localhost:8000/api/sensor-data"
              className="bg-slate-900/50 border border-slate-700/50 text-white rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent placeholder:text-slate-500"
              {...register('apiEndpoint')}
            />
            {errors.apiEndpoint && (
              <p className="text-sm text-red-400">
                {errors.apiEndpoint.message}
              </p>
            )}
          </div>

          <DialogFooter className="border-t border-slate-700/50 pt-4 mt-6">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={updateConfig.isPending}
              className="bg-slate-700/50 hover:bg-slate-600/50 text-white border border-slate-600 transition-all duration-200"
            >
              취소
            </Button>
            <Button
              type="submit"
              disabled={updateConfig.isPending}
              className="bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 text-white px-4 py-2 rounded-lg font-medium shadow-lg shadow-blue-500/50 transition-all duration-200"
            >
              {updateConfig.isPending ? '수정 중...' : '저장'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
