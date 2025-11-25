import { useState } from 'react';
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
import { useCreateDcpConfig } from '../../hooks/useDcpConfigs';
import { useMachines } from '../../hooks/useMachines';
import { useToast } from '../../hooks/useToast';

const dcpConfigSchema = z.object({
  machineId: z.number().min(1, '설비 선택은 필수입니다'),
  collectInterval: z.number().min(1, '수집 주기는 최소 1초 이상이어야 합니다'),
  apiEndpoint: z.string().url('유효하지 않은 URL입니다'),
});

type DcpConfigFormData = z.infer<typeof dcpConfigSchema>;

interface DcpConfigCreateModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function DcpConfigCreateModal({
  open,
  onOpenChange,
}: DcpConfigCreateModalProps) {
  const { toast } = useToast();
  const createConfig = useCreateDcpConfig();
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

  const onSubmit = async (data: DcpConfigFormData) => {
    try {
      await createConfig.mutateAsync(data);
      toast({
        title: '성공',
        description: 'DCP 설정이 생성되었습니다',
      });
      reset();
      setSelectedMachineId('');
      onOpenChange(false);
    } catch (error) {
      toast({
        title: '오류',
        description: 'DCP 설정 생성에 실패했습니다',
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
          <DialogTitle className="text-white text-xl">DCP 설정 생성</DialogTitle>
          <DialogDescription className="text-slate-400">
            설비의 데이터 수집 파라미터를 설정합니다
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-4">
          <div className="space-y-2">
            <Label htmlFor="machineId" className="text-slate-300">설비</Label>
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
            <Label htmlFor="collectInterval" className="text-slate-300">수집 주기 (초)</Label>
            <Input
              id="collectInterval"
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
            <Label htmlFor="apiEndpoint" className="text-slate-300">API 엔드포인트</Label>
            <Input
              id="apiEndpoint"
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
              disabled={createConfig.isPending}
              className="bg-slate-700/50 hover:bg-slate-600/50 text-white border border-slate-600 transition-all duration-200"
            >
              취소
            </Button>
            <Button
              type="submit"
              disabled={createConfig.isPending}
              className="bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 text-white px-4 py-2 rounded-lg font-medium shadow-lg shadow-blue-500/50 transition-all duration-200"
            >
              {createConfig.isPending ? '생성 중...' : '생성'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
