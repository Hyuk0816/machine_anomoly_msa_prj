import { useState } from 'react';
import { useDcpConfigs } from '../../hooks/useDcpConfigs';
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
import { DcpConfigCreateModal } from './DcpConfigCreateModal';
import { DcpConfigEditModal } from './DcpConfigEditModal';
import { DcpConfigDeleteDialog } from './DcpConfigDeleteDialog';
import type { DcpConfigResponseDto } from '../../types/api';
import { Edit, Trash2, Plus, Loader2, Clock } from 'lucide-react';

export function DcpConfigList() {
  const { data: configs, isLoading, error } = useDcpConfigs();
  const { data: machines } = useMachines();
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedConfig, setSelectedConfig] = useState<DcpConfigResponseDto | null>(null);

  const getMachineName = (machineId: number) => {
    const machine = machines?.find((m) => m.id === machineId);
    return machine?.name || `설비 #${machineId}`;
  };

  const handleEdit = (config: DcpConfigResponseDto) => {
    setSelectedConfig(config);
    setEditModalOpen(true);
  };

  const handleDelete = (config: DcpConfigResponseDto) => {
    setSelectedConfig(config);
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
        <p className="text-red-400">DCP 설정 목록을 불러올 수 없습니다</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <Card className="bg-slate-800/50 backdrop-blur-sm border border-slate-700/50 rounded-xl">
        <CardHeader className="bg-gradient-to-r from-blue-600/10 to-transparent border-b border-slate-700/50 p-6">
          <div className="flex items-center justify-between">
            <CardTitle className="text-white text-2xl font-semibold">DCP 설정 관리</CardTitle>
            <Button
              onClick={() => setCreateModalOpen(true)}
              className="bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 text-white px-4 py-2 rounded-lg font-medium shadow-lg shadow-blue-500/50 transition-all duration-200"
            >
              <Plus className="h-4 w-4 mr-2" />
              설정 추가
            </Button>
          </div>
        </CardHeader>
        <CardContent className="p-6">
          {configs && configs.length > 0 ? (
            <div className="rounded-lg overflow-hidden border border-slate-700/50">
              <Table>
                <TableHeader>
                  <TableRow className="bg-slate-900/50 border-b border-slate-700/50 hover:bg-slate-900/50">
                    <TableHead className="text-slate-300 font-semibold">ID</TableHead>
                    <TableHead className="text-slate-300 font-semibold">설비</TableHead>
                    <TableHead className="text-slate-300 font-semibold">수집 주기</TableHead>
                    <TableHead className="text-slate-300 font-semibold">API 엔드포인트</TableHead>
                    <TableHead className="text-right text-slate-300 font-semibold">작업</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {configs.map((config) => (
                    <TableRow
                      key={config.id}
                      className="bg-slate-900/30 hover:bg-slate-800/50 border-b border-slate-800/30 transition-all duration-200"
                    >
                      <TableCell className="font-medium text-white">{config.id}</TableCell>
                      <TableCell className="text-slate-300">{getMachineName(config.machineId)}</TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Clock className="h-4 w-4 text-blue-400" />
                          <span className="text-slate-300">{config.collectInterval}초</span>
                        </div>
                      </TableCell>
                      <TableCell className="max-w-xs">
                        <code className="text-xs text-green-400 bg-slate-900/50 px-2 py-1 rounded border border-slate-700/30">
                          {config.apiEndpoint}
                        </code>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleEdit(config)}
                            className="bg-slate-700/50 hover:bg-slate-600/50 text-white border border-slate-600 transition-all duration-200"
                          >
                            <Edit className="h-4 w-4" />
                          </Button>
                          <Button
                            variant="destructive"
                            size="sm"
                            onClick={() => handleDelete(config)}
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
              <p className="text-slate-400 mb-4">등록된 설정이 없습니다</p>
              <Button
                variant="outline"
                onClick={() => setCreateModalOpen(true)}
                className="bg-slate-700/50 hover:bg-slate-600/50 text-white border border-slate-600 transition-all duration-200"
              >
                <Plus className="h-4 w-4 mr-2" />
                첫 번째 설정 추가하기
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      <DcpConfigCreateModal
        open={createModalOpen}
        onOpenChange={setCreateModalOpen}
      />

      <DcpConfigEditModal
        open={editModalOpen}
        onOpenChange={setEditModalOpen}
        config={selectedConfig}
      />

      <DcpConfigDeleteDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        config={selectedConfig}
      />
    </div>
  );
}
