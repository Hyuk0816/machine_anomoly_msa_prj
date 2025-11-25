import { useEffect, useRef } from 'react';
import uPlot from 'uplot';
import 'uplot/dist/uPlot.min.css';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
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

  useEffect(() => {
    if (!chartRef.current || !data || data.length === 0) return;

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
          points: {
            show: data.length < 100,
          },
        },
      ],
      axes: [
        {
          label: 'Time',
          space: 80,
          values: (_, ticks) =>
            ticks.map((t) =>
              new Date(t * 1000).toLocaleString('en-US', {
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
  }, [data, dataKey, title, color, unit]);

  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <div ref={chartRef} className="w-full" />
      </CardContent>
    </Card>
  );
}
