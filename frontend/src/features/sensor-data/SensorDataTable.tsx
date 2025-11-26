import { useState } from 'react';
import { Card, CardContent } from '../../components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../../components/ui/table';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../components/ui/select';
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';
import type { MachineSensorDataResponseDto } from '../../types/api';

interface SensorDataTableProps {
  data: MachineSensorDataResponseDto[];
}

const PAGE_SIZE_OPTIONS = [5, 20, 50, 100];

export function SensorDataTable({ data }: SensorDataTableProps) {
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);

  const totalItems = data.length;
  const totalPages = Math.ceil(totalItems / pageSize);
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = Math.min(startIndex + pageSize, totalItems);
  const currentData = data.slice(startIndex, endIndex);

  const handlePageSizeChange = (value: string) => {
    setPageSize(Number(value));
    setCurrentPage(1);
  };

  const goToPage = (page: number) => {
    setCurrentPage(Math.max(1, Math.min(page, totalPages)));
  };

  return (
    <Card className="bg-white dark:bg-slate-800/50 backdrop-blur-sm border border-slate-200 dark:border-slate-700/50 rounded-xl shadow-sm">
      <CardContent className="p-6">
        {/* 페이지 크기 선택 및 정보 */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <span className="text-sm text-slate-600 dark:text-slate-400">페이지당</span>
            <Select value={pageSize.toString()} onValueChange={handlePageSizeChange}>
              <SelectTrigger className="w-20 h-8 bg-white dark:bg-slate-900/50 border border-slate-200 dark:border-slate-700/50 text-slate-900 dark:text-white rounded-lg">
                <SelectValue />
              </SelectTrigger>
              <SelectContent className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700/50">
                {PAGE_SIZE_OPTIONS.map((size) => (
                  <SelectItem
                    key={size}
                    value={size.toString()}
                    className="text-slate-900 dark:text-white hover:bg-slate-100 dark:hover:bg-slate-800 focus:bg-slate-100 dark:focus:bg-slate-800"
                  >
                    {size}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <span className="text-sm text-slate-600 dark:text-slate-400">개씩 보기</span>
          </div>
          <div className="text-sm text-slate-600 dark:text-slate-400">
            총 {totalItems}개 중 {startIndex + 1} - {endIndex}개 표시
          </div>
        </div>

        {/* 테이블 */}
        <div className="rounded-lg overflow-hidden border border-slate-200 dark:border-slate-700/50">
          <div className="overflow-x-auto">
            <Table>
              <TableHeader className="sticky top-0 bg-slate-50 dark:bg-slate-900/95 backdrop-blur-sm z-10">
                <TableRow className="border-b border-slate-200 dark:border-slate-700/50 hover:bg-slate-50 dark:hover:bg-slate-900/95">
                  <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">ID</TableHead>
                  <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">생성 일시</TableHead>
                  <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">공기 온도 (°C)</TableHead>
                  <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">공정 온도 (°C)</TableHead>
                  <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">회전 속도 (RPM)</TableHead>
                  <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">토크 (Nm)</TableHead>
                  <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">공구 마모 (min)</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {currentData.map((row) => (
                  <TableRow
                    key={row.id}
                    className="bg-white dark:bg-slate-900/30 hover:bg-slate-50 dark:hover:bg-slate-800/50 border-b border-slate-100 dark:border-slate-800/30 transition-all duration-200"
                  >
                    <TableCell className="font-medium text-slate-900 dark:text-white">{row.id}</TableCell>
                    <TableCell className="text-slate-600 dark:text-slate-300">
                      {new Date(row.createdAt).toLocaleString('ko-KR')}
                    </TableCell>
                    <TableCell className="text-slate-600 dark:text-slate-300">
                      {row.airTemperature?.toFixed(2) ?? '-'}
                    </TableCell>
                    <TableCell className="text-slate-600 dark:text-slate-300">
                      {row.processTemperature?.toFixed(2) ?? '-'}
                    </TableCell>
                    <TableCell className="text-slate-600 dark:text-slate-300">
                      {row.rotationalSpeed?.toFixed(2) ?? '-'}
                    </TableCell>
                    <TableCell className="text-slate-600 dark:text-slate-300">
                      {row.torque?.toFixed(2) ?? '-'}
                    </TableCell>
                    <TableCell className="text-slate-600 dark:text-slate-300">
                      {row.toolWear?.toFixed(2) ?? '-'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </div>

        {/* 페이지네이션 */}
        <div className="flex items-center justify-center gap-2 mt-4">
          <button
            onClick={() => goToPage(1)}
            disabled={currentPage === 1}
            className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800/50 hover:bg-slate-200 dark:hover:bg-slate-700/50 border border-slate-200 dark:border-slate-700/50 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
            aria-label="첫 페이지"
          >
            <ChevronsLeft className="h-4 w-4 text-slate-600 dark:text-slate-400" />
          </button>
          <button
            onClick={() => goToPage(currentPage - 1)}
            disabled={currentPage === 1}
            className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800/50 hover:bg-slate-200 dark:hover:bg-slate-700/50 border border-slate-200 dark:border-slate-700/50 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
            aria-label="이전 페이지"
          >
            <ChevronLeft className="h-4 w-4 text-slate-600 dark:text-slate-400" />
          </button>

          <div className="flex items-center gap-1">
            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
              let pageNum: number;
              if (totalPages <= 5) {
                pageNum = i + 1;
              } else if (currentPage <= 3) {
                pageNum = i + 1;
              } else if (currentPage >= totalPages - 2) {
                pageNum = totalPages - 4 + i;
              } else {
                pageNum = currentPage - 2 + i;
              }
              return (
                <button
                  key={pageNum}
                  onClick={() => goToPage(pageNum)}
                  className={`min-w-[36px] h-9 px-3 rounded-lg font-medium text-sm transition-all duration-200 ${
                    currentPage === pageNum
                      ? 'bg-blue-600 text-white shadow-lg shadow-blue-500/50'
                      : 'bg-slate-100 dark:bg-slate-800/50 text-slate-600 dark:text-slate-400 hover:bg-slate-200 dark:hover:bg-slate-700/50 border border-slate-200 dark:border-slate-700/50'
                  }`}
                >
                  {pageNum}
                </button>
              );
            })}
          </div>

          <button
            onClick={() => goToPage(currentPage + 1)}
            disabled={currentPage === totalPages}
            className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800/50 hover:bg-slate-200 dark:hover:bg-slate-700/50 border border-slate-200 dark:border-slate-700/50 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
            aria-label="다음 페이지"
          >
            <ChevronRight className="h-4 w-4 text-slate-600 dark:text-slate-400" />
          </button>
          <button
            onClick={() => goToPage(totalPages)}
            disabled={currentPage === totalPages}
            className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800/50 hover:bg-slate-200 dark:hover:bg-slate-700/50 border border-slate-200 dark:border-slate-700/50 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
            aria-label="마지막 페이지"
          >
            <ChevronsRight className="h-4 w-4 text-slate-600 dark:text-slate-400" />
          </button>
        </div>
      </CardContent>
    </Card>
  );
}