import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog';
import { Button } from '../../components/ui/button';
import { useDeleteMachine } from '../../hooks/useMachines';
import { useToast } from '../../hooks/useToast';
import type { MachineResponseDto } from '../../types/api';
import { AlertTriangle } from 'lucide-react';

interface MachineDeleteDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  machine: MachineResponseDto | null;
}

export function MachineDeleteDialog({
  open,
  onOpenChange,
  machine,
}: MachineDeleteDialogProps) {
  const { toast } = useToast();
  const deleteMachine = useDeleteMachine();

  const handleDelete = async () => {
    if (!machine) return;

    try {
      await deleteMachine.mutateAsync(machine.id);
      toast({
        title: '성공',
        description: '설비가 삭제되었습니다',
      });
      onOpenChange(false);
    } catch (error) {
      toast({
        title: '오류',
        description: '설비 삭제에 실패했습니다',
        variant: 'destructive',
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="bg-slate-900 border border-slate-700/50 rounded-xl shadow-2xl">
        <DialogHeader className="border-b border-slate-700/50 pb-4">
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-10 h-10 rounded-full bg-red-500/20 border border-red-500/30">
              <AlertTriangle className="h-5 w-5 text-red-400" />
            </div>
            <div>
              <DialogTitle className="text-white text-xl">설비 삭제</DialogTitle>
              <DialogDescription className="text-slate-400 mt-1">
                정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        {machine && (
          <div className="py-4 space-y-2 bg-slate-800/30 rounded-lg p-4 border border-slate-700/30">
            <p className="text-sm text-slate-300">
              <span className="font-medium text-slate-200">설비:</span> {machine.name}
            </p>
            <p className="text-sm text-slate-300">
              <span className="font-medium text-slate-200">유형:</span> {machine.type}
            </p>
          </div>
        )}

        <DialogFooter className="border-t border-slate-700/50 pt-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={deleteMachine.isPending}
            className="bg-slate-700/50 hover:bg-slate-600/50 text-white border border-slate-600 transition-all duration-200"
          >
            취소
          </Button>
          <Button
            type="button"
            variant="destructive"
            onClick={handleDelete}
            disabled={deleteMachine.isPending}
            className="bg-gradient-to-r from-red-600 to-red-500 hover:from-red-700 hover:to-red-600 text-white px-4 py-2 rounded-lg font-medium shadow-lg shadow-red-500/50 transition-all duration-200"
          >
            {deleteMachine.isPending ? '삭제 중...' : '삭제'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
