import { useEffect, useRef } from 'react';
import uPlot from 'uplot';
import 'uplot/dist/uPlot.min.css';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { useTheme } from '../../contexts/ThemeContext';
import type { MachineSensorDataResponseDto } from '../../types/api';

interface SensorChartProps {
  title: string;
  data: MachineSensorDataResponseDto[];
  dataKey: keyof Omit<MachineSensorDataResponseDto, 'id' | 'machineId' | 'createdAt'>;
  color: string;
  unit: string;
}

export function SensorChart({ title, data, dataKey, color, unit }: SensorChartProps) {
  const chartRef = useRef<HTMLDivElement>(null);
  const plotRef = useRef<uPlot | null>(null);
  const { theme } = useTheme();

  useEffect(() => {
    if (!chartRef.current || !data || data.length === 0) return;

    // 테마에 따른 색상 설정
    const isDark = theme === 'dark';
    const gridColor = isDark ? 'rgba(148, 163, 184, 0.2)' : 'rgba(100, 116, 139, 0.2)';
    const axisColor = isDark ? '#94a3b8' : '#475569';
    const bgColor = isDark ? '#1e293b' : '#ffffff';

    // Prepare data for uPlot
    const timestamps = data.map((d) => new Date(d.createdAt).getTime() / 1000);
    const values = data.map((d) => d[dataKey] as number);

    const plotData: uPlot.AlignedData = [timestamps, values];

    const options: uPlot.Options = {
      width: chartRef.current.clientWidth,
      height: 300,
      series: [
        {
          label: 'Time',
        },
        {
          label: title,
          stroke: color,
          width: 2,
          fill: `${color}20`,
          points: {
            show: data.length < 100,
          },
        },
      ],
      axes: [
        {
          label: 'Time',
          space: 80,
          stroke: axisColor,
          grid: {
            stroke: gridColor,
            width: 1,
          },
          ticks: {
            stroke: gridColor,
            width: 1,
          },
          values: (_, ticks) =>
            ticks.map((t) =>
              new Date(t * 1000).toLocaleString('ko-KR', {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              })
            ),
        },
        {
          label: `${title} (${unit})`,
          space: 60,
          stroke: axisColor,
          grid: {
            stroke: gridColor,
            width: 1,
          },
          ticks: {
            stroke: gridColor,
            width: 1,
          },
          values: (_, ticks) => ticks.map((t) => t.toFixed(2)),
        },
      ],
      cursor: {
        drag: {
          x: true,
          y: false,
        },
      },
      scales: {
        x: {
          time: true,
        },
      },
    };

    // 차트 배경색 설정을 위한 CSS
    if (chartRef.current) {
      chartRef.current.style.backgroundColor = bgColor;
      chartRef.current.style.borderRadius = '8px';
      chartRef.current.style.padding = '8px';
    }

    // Destroy existing plot
    if (plotRef.current) {
      plotRef.current.destroy();
    }

    // Create new plot
    plotRef.current = new uPlot(options, plotData, chartRef.current);

    // Handle window resize
    const handleResize = () => {
      if (plotRef.current && chartRef.current) {
        plotRef.current.setSize({
          width: chartRef.current.clientWidth,
          height: 300,
        });
      }
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      if (plotRef.current) {
        plotRef.current.destroy();
        plotRef.current = null;
      }
    };
  }, [data, dataKey, title, color, unit, theme]);

  return (
    <Card className="bg-white dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700/50 shadow-sm">
      <CardHeader>
        <CardTitle className="text-slate-900 dark:text-white">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <div ref={chartRef} className="w-full transition-colors duration-300" />
      </CardContent>
    </Card>
  );
}
