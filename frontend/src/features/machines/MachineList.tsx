import { useState } from 'react';
import { useMachines } from '../../hooks/useMachines';
import { Button } from '../../components/ui/button';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../../components/ui/table';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { MachineCreateModal } from './MachineCreateModal';
import { MachineEditModal } from './MachineEditModal';
import { MachineDeleteDialog } from './MachineDeleteDialog';
import type { MachineResponseDto } from '../../types/api';
import { Edit, Trash2, Plus, Loader2 } from 'lucide-react';

export function MachineList() {
  const { data: machines, isLoading, error } = useMachines();
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedMachine, setSelectedMachine] = useState<MachineResponseDto | null>(null);

  const handleEdit = (machine: MachineResponseDto) => {
    setSelectedMachine(machine);
    setEditModalOpen(true);
  };

  const handleDelete = (machine: MachineResponseDto) => {
    setSelectedMachine(machine);
    setDeleteDialogOpen(true);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="h-8 w-8 animate-spin text-blue-500" />
          <p className="text-slate-400">로딩 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-red-400">설비 목록을 불러올 수 없습니다</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <Card className="bg-slate-800/50 backdrop-blur-sm border border-slate-700/50 rounded-xl">
        <CardHeader className="bg-gradient-to-r from-blue-600/10 to-transparent border-b border-slate-700/50 p-6">
          <div className="flex items-center justify-between">
            <CardTitle className="text-white text-2xl font-semibold">설비 관리</CardTitle>
            <Button
              onClick={() => setCreateModalOpen(true)}
              className="bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 text-white px-4 py-2 rounded-lg font-medium shadow-lg shadow-blue-500/50 transition-all duration-200"
            >
              <Plus className="h-4 w-4 mr-2" />
              설비 추가
            </Button>
          </div>
        </CardHeader>
        <CardContent className="p-6">
          {machines && machines.length > 0 ? (
            <div className="rounded-lg overflow-hidden border border-slate-700/50">
              <Table>
                <TableHeader>
                  <TableRow className="bg-slate-900/50 border-b border-slate-700/50 hover:bg-slate-900/50">
                    <TableHead className="text-slate-300 font-semibold">ID</TableHead>
                    <TableHead className="text-slate-300 font-semibold">설비 이름</TableHead>
                    <TableHead className="text-slate-300 font-semibold">설비 유형</TableHead>
                    <TableHead className="text-right text-slate-300 font-semibold">작업</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {machines.map((machine) => (
                    <TableRow
                      key={machine.id}
                      className="bg-slate-900/30 hover:bg-slate-800/50 border-b border-slate-800/30 transition-all duration-200"
                    >
                      <TableCell className="font-medium text-white">{machine.id}</TableCell>
                      <TableCell className="text-slate-300">{machine.name}</TableCell>
                      <TableCell>
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-gradient-to-r from-blue-600/20 to-blue-500/20 text-blue-400 border border-blue-500/30">
                          {machine.type}
                        </span>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleEdit(machine)}
                            className="bg-slate-700/50 hover:bg-slate-600/50 text-white border border-slate-600 transition-all duration-200"
                          >
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="destructive"
                            size="sm"
                            onClick={() => handleDelete(machine)}
                            className="bg-gradient-to-r from-red-600/80 to-red-500/80 hover:from-red-700 hover:to-red-600 text-white border-0 transition-all duration-200"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          ) : (
            <div className="text-center py-16">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-slate-700/50 mb-4">
                <Plus className="h-8 w-8 text-slate-400" />
              </div>
              <p className="text-slate-400 mb-4">등록된 설비가 없습니다</p>
              <Button
                variant="outline"
                onClick={() => setCreateModalOpen(true)}
                className="bg-slate-700/50 hover:bg-slate-600/50 text-white border border-slate-600 transition-all duration-200"
              >
                <Plus className="h-4 w-4 mr-2" />
                첫 번째 설비 추가하기
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      <MachineCreateModal
        open={createModalOpen}
        onOpenChange={setCreateModalOpen}
      />

      <MachineEditModal
        open={editModalOpen}
        onOpenChange={setEditModalOpen}
        machine={selectedMachine}
      />

      <MachineDeleteDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        machine={selectedMachine}
      />
    </div>
  );
}
