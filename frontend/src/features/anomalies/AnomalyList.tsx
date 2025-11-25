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
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Button } from '../../components/ui/button';
import { formatDateTime, formatProbability, getProbabilityColor } from '../../utils/formatters';
import { Search, Loader2, AlertTriangle, X } from 'lucide-react';

export function AnomalyList() {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [isSearching, setIsSearching] = useState(false);

  const { data: allAnomalies, isLoading: allLoading } = useAnomalies();
  const {
    data: searchedAnomalies,
    isLoading: searchLoading,
    refetch,
  } = useAnomalySearch(startDate, endDate);

  const anomalies = isSearching ? searchedAnomalies : allAnomalies;
  const isLoading = isSearching ? searchLoading : allLoading;

  const handleSearch = () => {
    if (startDate && endDate) {
      setIsSearching(true);
      refetch();
    }
  };

  const handleClearSearch = () => {
    setStartDate('');
    setEndDate('');
    setIsSearching(false);
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
      <h1 className="text-3xl font-bold text-white">이상 탐지 내역</h1>

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

      <Card className="bg-slate-800/50 backdrop-blur-sm border border-slate-700/50 rounded-xl">
        <CardHeader className="bg-gradient-to-r from-red-600/10 to-transparent border-b border-slate-700/50 p-6">
          <div className="flex items-center justify-between">
            <CardTitle className="text-white text-xl flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-red-400" />
              탐지된 이상
              {anomalies && (
                <span className="text-sm font-normal text-slate-400 ml-2">
                  (총 {anomalies.length}건)
                </span>
              )}
            </CardTitle>
          </div>
        </CardHeader>
        <CardContent className="p-6">
          {anomalies && anomalies.length > 0 ? (
            <div className="rounded-lg overflow-hidden border border-slate-700/50">
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow className="bg-slate-900/50 border-b border-slate-700/50 hover:bg-slate-900/50">
                      <TableHead className="text-slate-300 font-semibold">ID</TableHead>
                      <TableHead className="text-slate-300 font-semibold">설비 ID</TableHead>
                      <TableHead className="text-slate-300 font-semibold">탐지 시각</TableHead>
                      <TableHead className="text-slate-300 font-semibold">확률</TableHead>
                      <TableHead className="text-slate-300 font-semibold">공기 온도</TableHead>
                      <TableHead className="text-slate-300 font-semibold">공정 온도</TableHead>
                      <TableHead className="text-slate-300 font-semibold">회전 속도</TableHead>
                      <TableHead className="text-slate-300 font-semibold">토크</TableHead>
                      <TableHead className="text-slate-300 font-semibold">공구 마모</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {anomalies.map((anomaly) => (
                      <TableRow
                        key={anomaly.id}
                        className="bg-slate-900/30 hover:bg-slate-800/50 border-b border-slate-800/30 transition-all duration-200"
                      >
                        <TableCell className="font-medium text-white">{anomaly.id}</TableCell>
                        <TableCell className="text-slate-300">{anomaly.machineId}</TableCell>
                        <TableCell className="text-slate-300">{formatDateTime(anomaly.detectedAt)}</TableCell>
                        <TableCell>
                          <span className={getProbabilityColor(anomaly.anomalyProbability)}>
                            {formatProbability(anomaly.anomalyProbability)}
                          </span>
                        </TableCell>
                        <TableCell className="text-slate-300">
                          {anomaly.eventMessageSensorData.airTemperature.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-slate-300">
                          {anomaly.eventMessageSensorData.processTemperature.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-slate-300">
                          {anomaly.eventMessageSensorData.rotationalSpeed.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-slate-300">
                          {anomaly.eventMessageSensorData.torque.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-slate-300">
                          {anomaly.eventMessageSensorData.toolWear.toFixed(2)}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </div>
          ) : (
            <div className="text-center py-16">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-slate-700/50 mb-4">
                <AlertTriangle className="h-8 w-8 text-slate-400" />
              </div>
              <p className="text-slate-400">
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
