import { Link, useLocation } from 'react-router-dom';
import { cn } from '../../utils/cn';
import { Factory, LayoutDashboard, Settings, Database, AlertTriangle, LineChart, Sun, Moon } from 'lucide-react';
import { useTheme } from '../../contexts/ThemeContext';

const navigation = [
  { name: '대시보드', href: '/', icon: LayoutDashboard },
  { name: '설비 관리', href: '/machines', icon: Factory },
  { name: 'DCP 설정', href: '/dcp-configs', icon: Settings },
  { name: '센서 데이터', href: '/sensor-data', icon: LineChart },
  { name: '이상 탐지 내역', href: '/anomalies', icon: AlertTriangle },
];

export function Navbar() {
  const location = useLocation();
  const { theme, toggleTheme } = useTheme();

  return (
    <nav className="bg-gradient-to-r from-white via-slate-50 to-white dark:from-slate-900 dark:via-slate-800 dark:to-slate-900 border-b border-slate-200 dark:border-slate-700/50 backdrop-blur-sm transition-colors duration-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center">
            <div className="flex-shrink-0 flex items-center gap-3">
              <div className="relative">
                <div className="absolute inset-0 bg-blue-500 blur-md opacity-50"></div>
                <Database className="relative h-8 w-8 text-blue-400" />
              </div>
              <div>
                <span className="text-slate-900 dark:text-white text-xl font-bold tracking-tight">
                  스마트 팩토리
                </span>
                <span className="block text-xs text-blue-600 dark:text-blue-400 font-medium">
                   실시간 이상탐지 시스템
                </span>
              </div>
            </div>
            <div className="ml-10 hidden md:block">
              <div className="flex items-baseline space-x-2">
                {navigation.map((item) => {
                  const isActive = location.pathname === item.href;
                  const Icon = item.icon;
                  return (
                    <Link
                      key={item.name}
                      to={item.href}
                      className={cn(
                        'flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200',
                        isActive
                          ? 'bg-blue-600 text-white shadow-lg shadow-blue-500/50'
                          : 'text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700/50 hover:text-slate-900 dark:hover:text-white'
                      )}
                    >
                      <Icon className="h-4 w-4" />
                      {item.name}
                    </Link>
                  );
                })}
              </div>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <button
              onClick={toggleTheme}
              className="p-2 rounded-lg bg-slate-200 dark:bg-slate-800/50 hover:bg-slate-300 dark:hover:bg-slate-700/50 border border-slate-300 dark:border-slate-700/50 transition-all duration-200"
              aria-label="테마 전환"
            >
              {theme === 'dark' ? (
                <Sun className="h-5 w-5 text-yellow-500" />
              ) : (
                <Moon className="h-5 w-5 text-slate-600" />
              )}
            </button>
            <div className="flex items-center gap-2">
              <div className="h-2 w-2 rounded-full bg-green-500 dark:bg-green-400 animate-pulse"></div>
              <span className="text-xs text-slate-600 dark:text-slate-400">실시간 모니터링</span>
            </div>
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      <div className="md:hidden border-t border-slate-200 dark:border-slate-700/50">
        <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
          {navigation.map((item) => {
            const isActive = location.pathname === item.href;
            const Icon = item.icon;
            return (
              <Link
                key={item.name}
                to={item.href}
                className={cn(
                  'flex items-center gap-3 px-3 py-2 rounded-md text-base font-medium transition-colors',
                  isActive
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700/50 hover:text-slate-900 dark:hover:text-white'
                )}
              >
                <Icon className="h-5 w-5" />
                {item.name}
              </Link>
            );
          })}
        </div>
      </div>
    </nav>
  );
}
