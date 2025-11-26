import { useState } from 'react';
import { useAnomalies, useAnomalySearch } from '../../hooks/useAnomalies';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
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
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Button } from '../../components/ui/button';
import { formatDateTime, formatProbability, getProbabilityColor, getSeverityColor, getSeverityLabel } from '../../utils/formatters';
import { Search, Loader2, AlertTriangle, X, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';

const PAGE_SIZE_OPTIONS = [5, 20, 50, 100];

// 날짜를 YYYY-MM-DDTHH:MM 형식으로 변환하는 헬퍼 함수
const formatDateTimeLocal = (date: Date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};

export function AnomalyList() {
  // 기본값: 시작일시는 일주일 전, 종료일시는 현재
  const [startDate, setStartDate] = useState(() => {
    const weekAgo = new Date();
    weekAgo.setDate(weekAgo.getDate() - 7);
    return formatDateTimeLocal(weekAgo);
  });

  const [endDate, setEndDate] = useState(() => {
    return formatDateTimeLocal(new Date());
  });

  const [isSearching, setIsSearching] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);

  const { data: allAnomalies, isLoading: allLoading } = useAnomalies();
  const {
    data: searchedAnomalies,
    isLoading: searchLoading,
    refetch,
  } = useAnomalySearch(startDate, endDate);

  const anomalies = isSearching ? searchedAnomalies : allAnomalies;
  const isLoading = isSearching ? searchLoading : allLoading;

  // 페이징 계산
  const totalItems = anomalies?.length || 0;
  const totalPages = Math.ceil(totalItems / pageSize);
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = Math.min(startIndex + pageSize, totalItems);
  const currentData = anomalies?.slice(startIndex, endIndex) || [];

  const handlePageSizeChange = (value: string) => {
    setPageSize(Number(value));
    setCurrentPage(1);
  };

  const goToPage = (page: number) => {
    setCurrentPage(Math.max(1, Math.min(page, totalPages)));
  };

  const handleSearch = () => {
    if (startDate && endDate) {
      setIsSearching(true);
      setCurrentPage(1);
      refetch();
    }
  };

  const handleClearSearch = () => {
    setStartDate('');
    setEndDate('');
    setIsSearching(false);
    setCurrentPage(1);
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

  return (
    <div className="space-y-4">
      <h1 className="text-3xl font-bold text-slate-900 dark:text-white">이상 탐지 내역</h1>

      <Card className="bg-slate-800/50 backdrop-blur-sm border border-slate-700/50 rounded-xl">
        <CardHeader className="bg-gradient-to-r from-orange-600/10 to-transparent border-b border-slate-700/50 p-6">
          <CardTitle className="text-white text-xl">날짜 필터</CardTitle>
        </CardHeader>
        <CardContent className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label htmlFor="startDate" className="text-slate-300">시작 일시</Label>
              <Input
                id="startDate"
                type="datetime-local"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="bg-slate-900/50 border border-slate-700/50 text-white rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="endDate" className="text-slate-300">종료 일시</Label>
              <Input
                id="endDate"
                type="datetime-local"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="bg-slate-900/50 border border-slate-700/50 text-white rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div className="flex items-end gap-2">
              <Button
                onClick={handleSearch}
                disabled={!startDate || !endDate}
                className="bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 text-white px-4 py-2 rounded-lg font-medium shadow-lg shadow-blue-500/50 transition-all duration-200 disabled:opacity-50"
              >
                <Search className="h-4 w-4 mr-2" />
                검색
              </Button>
              {isSearching && (
                <Button
                  variant="outline"
                  onClick={handleClearSearch}
                  className="bg-slate-700/50 hover:bg-slate-600/50 text-white border border-slate-600 transition-all duration-200"
                >
                  <X className="h-4 w-4 mr-2" />
                  초기화
                </Button>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className="bg-white dark:bg-slate-800/50 backdrop-blur-sm border border-slate-200 dark:border-slate-700/50 rounded-xl shadow-sm">
        <CardHeader className="bg-gradient-to-r from-red-600/10 to-transparent border-b border-slate-200 dark:border-slate-700/50 p-6">
          <div className="flex items-center justify-between">
            <CardTitle className="text-slate-900 dark:text-white text-xl flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-red-500 dark:text-red-400" />
              탐지된 이상
              {anomalies && (
                <span className="text-sm font-normal text-slate-500 dark:text-slate-400 ml-2">
                  (총 {anomalies.length}건)
                </span>
              )}
            </CardTitle>
          </div>
        </CardHeader>
        <CardContent className="p-6">
          {anomalies && anomalies.length > 0 ? (
            <>
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

              <div className="rounded-lg overflow-hidden border border-slate-200 dark:border-slate-700/50">
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow className="bg-slate-50 dark:bg-slate-900/50 border-b border-slate-200 dark:border-slate-700/50 hover:bg-slate-50 dark:hover:bg-slate-900/50">
                        <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">ID</TableHead>
                        <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">설비 ID</TableHead>
                        <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">탐지 시각</TableHead>
                        <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">확률</TableHead>
                        <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">심각도</TableHead>
                        <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">공기 온도</TableHead>
                        <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">공정 온도</TableHead>
                        <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">회전 속도</TableHead>
                        <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">토크</TableHead>
                        <TableHead className="text-slate-700 dark:text-slate-300 font-semibold">공구 마모</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {currentData.map((anomaly) => (
                        <TableRow
                          key={anomaly.id}
                          className="bg-white dark:bg-slate-900/30 hover:bg-slate-50 dark:hover:bg-slate-800/50 border-b border-slate-100 dark:border-slate-800/30 transition-all duration-200"
                        >
                          <TableCell className="font-medium text-slate-900 dark:text-white">{anomaly.id}</TableCell>
                          <TableCell className="text-slate-600 dark:text-slate-300">{anomaly.machineId}</TableCell>
                          <TableCell className="text-slate-600 dark:text-slate-300">{formatDateTime(anomaly.detectedAt)}</TableCell>
                          <TableCell>
                            <span className={getProbabilityColor(anomaly.anomalyProbability)}>
                              {formatProbability(anomaly.anomalyProbability)}
                            </span>
                          </TableCell>
                          <TableCell>
                            <span className={getSeverityColor(anomaly.severity)}>
                              {getSeverityLabel(anomaly.severity)}
                            </span>
                          </TableCell>
                          <TableCell className="text-slate-600 dark:text-slate-300">
                            {anomaly.eventMessageSensorData.airTemperature?.toFixed(2) ?? '-'}
                          </TableCell>
                          <TableCell className="text-slate-600 dark:text-slate-300">
                            {anomaly.eventMessageSensorData.processTemperature?.toFixed(2) ?? '-'}
                          </TableCell>
                          <TableCell className="text-slate-600 dark:text-slate-300">
                            {anomaly.eventMessageSensorData.rotationalSpeed?.toFixed(2) ?? '-'}
                          </TableCell>
                          <TableCell className="text-slate-600 dark:text-slate-300">
                            {anomaly.eventMessageSensorData.torque?.toFixed(2) ?? '-'}
                          </TableCell>
                          <TableCell className="text-slate-600 dark:text-slate-300">
                            {anomaly.eventMessageSensorData.toolWear?.toFixed(2) ?? '-'}
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
            </>
          ) : (
            <div className="text-center py-16">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-700/50 mb-4">
                <AlertTriangle className="h-8 w-8 text-slate-400" />
              </div>
              <p className="text-slate-500 dark:text-slate-400">
                {isSearching
                  ? '선택한 기간에 이상 탐지 내역이 없습니다'
                  : '이상 탐지 내역이 없습니다'}
              </p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
