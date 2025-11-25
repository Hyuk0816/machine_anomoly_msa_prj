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
import { useUpdateMachine } from '../../hooks/useMachines';
import { useToast } from '../../hooks/useToast';
import { MACHINE_TYPES } from '../../types/api';
import type { MachineResponseDto } from '../../types/api';

const machineSchema = z.object({
  name: z.string().min(1, '이름은 필수입니다').max(100, '이름이 너무 깁니다'),
  type: z.string().min(1, '유형은 필수입니다'),
});

type MachineFormData = z.infer<typeof machineSchema>;

interface MachineEditModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  machine: MachineResponseDto | null;
}

export function MachineEditModal({
  open,
  onOpenChange,
  machine,
}: MachineEditModalProps) {
  const { toast } = useToast();
  const updateMachine = useUpdateMachine();
  const [selectedType, setSelectedType] = useState<string>('');

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    setValue,
  } = useForm<MachineFormData>({
    resolver: zodResolver(machineSchema),
  });

  useEffect(() => {
    if (machine) {
      setValue('name', machine.name);
      setValue('type', machine.type);
      setSelectedType(machine.type);
    }
  }, [machine, setValue]);

  const onSubmit = async (data: MachineFormData) => {
    if (!machine) return;

    try {
      await updateMachine.mutateAsync({
        id: machine.id,
        data: {
          name: data.name,
          type: data.type,
        },
      });
      toast({
        title: '성공',
        description: '설비가 수정되었습니다',
      });
      reset();
      setSelectedType('');
      onOpenChange(false);
    } catch (error) {
      toast({
        title: '오류',
        description: '설비 수정에 실패했습니다',
        variant: 'destructive',
      });
    }
  };

  const handleClose = () => {
    reset();
    setSelectedType('');
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="bg-slate-900 border border-slate-700/50 rounded-xl shadow-2xl">
        <DialogHeader className="border-b border-slate-700/50 pb-4">
          <DialogTitle className="text-white text-xl">설비 수정</DialogTitle>
          <DialogDescription className="text-slate-400">
            설비 정보를 수정합니다
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-4">
          <div className="space-y-2">
            <Label htmlFor="edit-name" className="text-slate-300">설비 이름</Label>
            <Input
              id="edit-name"
              placeholder="설비 이름을 입력하세요"
              className="bg-slate-900/50 border border-slate-700/50 text-white rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent placeholder:text-slate-500"
              {...register('name')}
            />
            {errors.name && (
              <p className="text-sm text-red-400">{errors.name.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="edit-type" className="text-slate-300">설비 유형</Label>
            <Select
              value={selectedType}
              onValueChange={(value) => {
                setSelectedType(value);
                setValue('type', value);
              }}
            >
              <SelectTrigger className="bg-slate-900/50 border border-slate-700/50 text-white rounded-lg">
                <SelectValue placeholder="설비 유형을 선택하세요" />
              </SelectTrigger>
              <SelectContent className="bg-slate-900 border border-slate-700/50">
                {MACHINE_TYPES.map((type) => (
                  <SelectItem
                    key={type}
                    value={type}
                    className="text-white hover:bg-slate-800 focus:bg-slate-800"
                  >
                    {type}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.type && (
              <p className="text-sm text-red-400">{errors.type.message}</p>
            )}
          </div>

          <DialogFooter className="border-t border-slate-700/50 pt-4 mt-6">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={updateMachine.isPending}
              className="bg-slate-700/50 hover:bg-slate-600/50 text-white border border-slate-600 transition-all duration-200"
            >
              취소
            </Button>
            <Button
              type="submit"
              disabled={updateMachine.isPending}
              className="bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 text-white px-4 py-2 rounded-lg font-medium shadow-lg shadow-blue-500/50 transition-all duration-200"
            >
              {updateMachine.isPending ? '수정 중...' : '저장'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
