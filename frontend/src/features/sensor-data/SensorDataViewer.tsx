import { useState } from 'react';
import { useMachines } from '../../hooks/useMachines';
import { useSensorData } from '../../hooks/useSensorData';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Button } from '../../components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../components/ui/select';
import { SensorChart } from './SensorChart';
import { Download, Search, Loader2, Database } from 'lucide-react';

export function SensorDataViewer() {
  const { data: machines } = useMachines();
  const [selectedMachineId, setSelectedMachineId] = useState<number | null>(null);
  const [startDateTime, setStartDateTime] = useState('');
  const [endDateTime, setEndDateTime] = useState('');
  const [hasSearched, setHasSearched] = useState(false);

  const { data: sensorData, isLoading } = useSensorData(
    selectedMachineId,
    startDateTime,
    endDateTime
  );

  const handleSearch = () => {
    setHasSearched(true);
  };

  const handleExportCSV = () => {
    if (!sensorData || sensorData.length === 0) return;

    const headers = [
      'ID',
      '설비 ID',
      '공기 온도',
      '공정 온도',
      '회전 속도',
      '토크',
      '공구 마모',
      '생성 일시',
    ];

    const csvContent = [
      headers.join(','),
      ...sensorData.map((row) =>
        [
          row.id,
          row.machineId,
          row.airTemperature,
          row.processTemperature,
          row.rotationalSpeed,
          row.torque,
          row.toolWear,
          row.createdAt,
        ].join(',')
      ),
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `sensor-data-machine-${selectedMachineId}-${Date.now()}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-4">
      <h1 className="text-3xl font-bold text-white">센서 데이터 조회</h1>

      <Card className="bg-slate-800/50 backdrop-blur-sm border border-slate-700/50 rounded-xl">
        <CardHeader className="bg-gradient-to-r from-blue-600/10 to-transparent border-b border-slate-700/50 p-6">
          <CardTitle className="text-white text-xl">조회 조건 선택</CardTitle>
        </CardHeader>
        <CardContent className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="space-y-2">
              <Label htmlFor="machine" className="text-slate-300">설비</Label>
              <Select
                value={selectedMachineId?.toString() || ''}
                onValueChange={(value) => setSelectedMachineId(parseInt(value))}
              >
                <SelectTrigger className="bg-slate-900/50 border border-slate-700/50 text-white rounded-lg">
                  <SelectValue placeholder="설비 선택" />
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
            </div>

            <div className="space-y-2">
              <Label htmlFor="startDateTime" className="text-slate-300">시작 일시</Label>
              <Input
                id="startDateTime"
                type="datetime-local"
                value={startDateTime}
                onChange={(e) => setStartDateTime(e.target.value)}
                className="bg-slate-900/50 border border-slate-700/50 text-white rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="endDateTime" className="text-slate-300">종료 일시</Label>
              <Input
                id="endDateTime"
                type="datetime-local"
                value={endDateTime}
                onChange={(e) => setEndDateTime(e.target.value)}
                className="bg-slate-900/50 border border-slate-700/50 text-white rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div className="flex items-end gap-2">
              <Button
                onClick={handleSearch}
                disabled={!selectedMachineId || !startDateTime || !endDateTime}
                className="bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-700 hover:to-blue-600 text-white px-4 py-2 rounded-lg font-medium shadow-lg shadow-blue-500/50 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Search className="h-4 w-4 mr-2" />
                데이터 조회
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {hasSearched && (
        <>
          {isLoading ? (
            <Card className="bg-slate-800/50 backdrop-blur-sm border border-slate-700/50 rounded-xl">
              <CardContent className="py-16">
                <div className="flex flex-col items-center gap-3">
                  <Loader2 className="h-8 w-8 animate-spin text-blue-500" />
                  <p className="text-center text-slate-400">센서 데이터 로딩 중...</p>
                </div>
              </CardContent>
            </Card>
          ) : sensorData && sensorData.length > 0 ? (
            <>
              <div className="flex justify-between items-center bg-slate-800/30 rounded-lg p-4 border border-slate-700/50">
                <h2 className="text-xl font-semibold text-white">
                  <Database className="inline h-5 w-5 mr-2 text-blue-400" />
                  {sensorData.length}개의 데이터 포인트
                </h2>
                <Button
                  variant="outline"
                  onClick={handleExportCSV}
                  className="bg-slate-700/50 hover:bg-slate-600/50 text-white border border-slate-600 transition-all duration-200"
                >
                  <Download className="h-4 w-4 mr-2" />
                  CSV 내보내기
                </Button>
              </div>

              <div className="grid grid-cols-1 gap-4">
                <SensorChart
                  title="공기 온도"
                  data={sensorData}
                  dataKey="airTemperature"
                  color="#3b82f6"
                  unit="°C"
                />
                <SensorChart
                  title="공정 온도"
                  data={sensorData}
                  dataKey="processTemperature"
                  color="#ef4444"
                  unit="°C"
                />
                <SensorChart
                  title="회전 속도"
                  data={sensorData}
                  dataKey="rotationalSpeed"
                  color="#10b981"
                  unit="RPM"
                />
                <SensorChart
                  title="토크"
                  data={sensorData}
                  dataKey="torque"
                  color="#f59e0b"
                  unit="Nm"
                />
                <SensorChart
                  title="공구 마모"
                  data={sensorData}
                  dataKey="toolWear"
                  color="#8b5cf6"
                  unit="min"
                />
              </div>
            </>
          ) : (
            <Card className="bg-slate-800/50 backdrop-blur-sm border border-slate-700/50 rounded-xl">
              <CardContent className="py-16">
                <div className="text-center">
                  <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-slate-700/50 mb-4">
                    <Database className="h-8 w-8 text-slate-400" />
                  </div>
                  <p className="text-center text-slate-400">
                    선택한 조건에 해당하는 센서 데이터가 없습니다
                  </p>
                </div>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </div>
  );
}
