import { useMachines } from '../../hooks/useMachines';
import { useDcpConfigs } from '../../hooks/useDcpConfigs';
import { useAnomalies } from '../../hooks/useAnomalies';
import { Activity, Settings, AlertTriangle, Database, Zap, CheckCircle2, XCircle } from 'lucide-react';

export function Dashboard() {
  const { data: machines, isLoading: machinesLoading } = useMachines();
  const { data: configs, isLoading: configsLoading } = useDcpConfigs();
  const { data: anomalies, isLoading: anomaliesLoading } = useAnomalies();

  // 날짜 기준 DESC 정렬하여 최근 5개 가져오기
  const recentAnomalies = anomalies
    ?.sort((a, b) => new Date(b.detectedAt).getTime() - new Date(a.detectedAt).getTime())
    .slice(0, 5) || [];
  const highProbabilityAnomalies =
    anomalies?.filter((a) => a.anomalyProbability > 0.7).length || 0;

  const stats = [
    {
      title: '전체 설비',
      value: machines?.length || 0,
      icon: Database,
      gradient: 'from-blue-600 to-blue-400',
      loading: machinesLoading,
    },
    {
      title: '활성 설정',
      value: configs?.length || 0,
      icon: Settings,
      gradient: 'from-emerald-600 to-emerald-400',
      loading: configsLoading,
    },
    {
      title: '총 이상 탐지',
      value: anomalies?.length || 0,
      icon: Activity,
      gradient: 'from-amber-600 to-amber-400',
      loading: anomaliesLoading,
    },
    {
      title: '고위험 이상',
      value: highProbabilityAnomalies,
      icon: AlertTriangle,
      gradient: 'from-red-600 to-red-400',
      loading: anomaliesLoading,
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white mb-1">실시간 모니터링 대시보드</h1>
          <p className="text-slate-500 dark:text-slate-400 text-sm">스마트 팩토리 예지보전 시스템</p>
        </div>
        <div className="flex items-center gap-3 bg-white dark:bg-slate-800/50 backdrop-blur-sm px-4 py-2 rounded-lg border border-slate-200 dark:border-slate-700/50 shadow-sm">
          <div className="flex items-center gap-2">
            <div className="h-2 w-2 rounded-full bg-green-500 dark:bg-green-400 animate-pulse"></div>
            <span className="text-xs text-slate-600 dark:text-slate-300">시스템 정상</span>
          </div>
          <div className="h-4 w-px bg-slate-300 dark:bg-slate-700"></div>
          <span className="text-xs text-slate-500 dark:text-slate-400">
            {new Date().toLocaleString('ko-KR')}
          </span>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div
              key={stat.title}
              className="relative overflow-hidden bg-white dark:bg-slate-800/50 backdrop-blur-sm border border-slate-200 dark:border-slate-700/50 rounded-xl p-6 hover:border-slate-300 dark:hover:border-slate-600 transition-all duration-300 group shadow-sm"
            >
              {/* Background gradient effect */}
              <div className={`absolute inset-0 bg-gradient-to-br ${stat.gradient} opacity-0 group-hover:opacity-10 transition-opacity duration-300`}></div>

              <div className="relative">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <p className="text-slate-500 dark:text-slate-400 text-sm mb-2">{stat.title}</p>
                    <p className="text-3xl font-bold text-slate-900 dark:text-white mb-1">
                      {stat.loading ? (
                        <span className="inline-block w-16 h-8 bg-slate-200 dark:bg-slate-700 animate-pulse rounded"></span>
                      ) : (
                        stat.value
                      )}
                    </p>
                  </div>
                  <div className={`bg-gradient-to-br ${stat.gradient} p-3 rounded-lg shadow-lg`}>
                    <Icon className="h-6 w-6 text-white" />
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Machine Status */}
        <div className="bg-white dark:bg-slate-800/50 backdrop-blur-sm border border-slate-200 dark:border-slate-700/50 rounded-xl overflow-hidden shadow-sm">
          <div className="bg-gradient-to-r from-blue-600/10 to-transparent border-b border-slate-200 dark:border-slate-700/50 p-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold text-slate-900 dark:text-white mb-1">설비 현황</h2>
                <p className="text-slate-500 dark:text-slate-400 text-sm">실시간 설비 연결 상태</p>
              </div>
              <Database className="h-8 w-8 text-blue-500 dark:text-blue-400" />
            </div>
          </div>
          <div className="p-6">
            {machinesLoading ? (
              <div className="space-y-3">
                {[...Array(3)].map((_, i) => (
                  <div key={i} className="h-16 bg-slate-100 dark:bg-slate-700/50 animate-pulse rounded-lg"></div>
                ))}
              </div>
            ) : machines && machines.length > 0 ? (
              <div className="space-y-3">
                {machines.map((machine) => {
                  const hasConfig = configs?.some((c) => c.machineId === machine.id);
                  return (
                    <div
                      key={machine.id}
                      className="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-900/50 rounded-lg border border-slate-200 dark:border-slate-700/30 hover:border-blue-500/50 transition-all duration-200 group"
                    >
                      <div className="flex items-center gap-4">
                        <div className={`p-2 rounded-lg ${hasConfig ? 'bg-green-500/20' : 'bg-slate-200 dark:bg-slate-700/50'}`}>
                          {hasConfig ? (
                            <CheckCircle2 className="h-5 w-5 text-green-500 dark:text-green-400" />
                          ) : (
                            <XCircle className="h-5 w-5 text-slate-400 dark:text-slate-500" />
                          )}
                        </div>
                        <div>
                          <p className="font-semibold text-slate-900 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                            {machine.name}
                          </p>
                          <p className="text-sm text-slate-500 dark:text-slate-400">{machine.type}</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <span
                          className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium ${
                            hasConfig
                              ? 'bg-green-500/20 text-green-600 dark:text-green-400 border border-green-500/30'
                              : 'bg-slate-200 dark:bg-slate-700/50 text-slate-500 dark:text-slate-400 border border-slate-300 dark:border-slate-600/30'
                          }`}
                        >
                          <div className={`h-1.5 w-1.5 rounded-full ${hasConfig ? 'bg-green-500 dark:bg-green-400' : 'bg-slate-400 dark:bg-slate-500'}`}></div>
                          {hasConfig ? '연결됨' : '미연결'}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="text-center py-12">
                <Database className="h-12 w-12 text-slate-400 dark:text-slate-600 mx-auto mb-3" />
                <p className="text-slate-500 dark:text-slate-400">설정된 설비가 없습니다</p>
              </div>
            )}
          </div>
        </div>

        {/* Recent Anomalies */}
        <div className="bg-white dark:bg-slate-800/50 backdrop-blur-sm border border-slate-200 dark:border-slate-700/50 rounded-xl overflow-hidden shadow-sm">
          <div className="bg-gradient-to-r from-red-600/10 to-transparent border-b border-slate-200 dark:border-slate-700/50 p-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold text-slate-900 dark:text-white mb-1">최근 이상 탐지</h2>
                <p className="text-slate-500 dark:text-slate-400 text-sm">최신순으로 표시</p>
              </div>
              <Zap className="h-8 w-8 text-amber-500 dark:text-amber-400" />
            </div>
          </div>
          <div className="p-6">
            {anomaliesLoading ? (
              <div className="space-y-3">
                {[...Array(5)].map((_, i) => (
                  <div key={i} className="h-16 bg-slate-100 dark:bg-slate-700/50 animate-pulse rounded-lg"></div>
                ))}
              </div>
            ) : recentAnomalies.length > 0 ? (
              <div className="space-y-3">
                {recentAnomalies.map((anomaly) => {
                  const probability = anomaly.anomalyProbability;
                  const isHigh = probability > 0.7;
                  const isMedium = probability > 0.5 && probability <= 0.7;

                  // 설비 이름 가져오기
                  const machine = machines?.find((m) => m.id === anomaly.machineId);
                  const machineName = machine?.name || `설비 #${anomaly.machineId}`;

                  return (
                    <div
                      key={anomaly.id}
                      className={`flex items-center justify-between p-4 rounded-lg border transition-all duration-200 ${
                        isHigh
                          ? 'bg-red-500/10 border-red-500/30 hover:border-red-500/50'
                          : isMedium
                          ? 'bg-orange-500/10 border-orange-500/30 hover:border-orange-500/50'
                          : 'bg-yellow-500/10 border-yellow-500/30 hover:border-yellow-500/50'
                      }`}
                    >
                      <div className="flex items-center gap-4 flex-1">
                        <div className={`p-2 rounded-lg ${
                          isHigh ? 'bg-red-500/20' : isMedium ? 'bg-orange-500/20' : 'bg-yellow-500/20'
                        }`}>
                          <AlertTriangle className={`h-5 w-5 ${
                            isHigh ? 'text-red-500 dark:text-red-400' : isMedium ? 'text-orange-500 dark:text-orange-400' : 'text-yellow-500 dark:text-yellow-400'
                          }`} />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="font-semibold text-slate-900 dark:text-white truncate">
                            {machineName}
                          </p>
                          <p className="text-sm text-slate-500 dark:text-slate-400">
                            {new Date(anomaly.detectedAt).toLocaleString('ko-KR')}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <span
                          className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-bold ${
                            isHigh
                              ? 'bg-red-500/20 text-red-600 dark:text-red-400 border border-red-500/30'
                              : isMedium
                              ? 'bg-orange-500/20 text-orange-600 dark:text-orange-400 border border-orange-500/30'
                              : 'bg-yellow-500/20 text-yellow-600 dark:text-yellow-400 border border-yellow-500/30'
                          }`}
                        >
                          {(probability * 100).toFixed(1)}%
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="text-center py-12">
                <CheckCircle2 className="h-12 w-12 text-green-500 mx-auto mb-3" />
                <p className="text-slate-500 dark:text-slate-400">이상 탐지된 항목이 없습니다</p>
                <p className="text-slate-400 dark:text-slate-500 text-sm mt-1">모든 설비가 정상 작동 중입니다</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
