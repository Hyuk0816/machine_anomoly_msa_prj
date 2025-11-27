import { useEffect, useRef } from 'react';
import { toast } from './useToast';
import { format } from 'date-fns';

interface AnomalyAlert {
  machineId: number;
  machineName: string;
  detectedAt: string;
  severity: 'WARNING' | 'ALERT' | 'CRITICAL';
  anomalyProbability: number;
}

const getSeverityLabel = (severity: string) => {
  switch (severity) {
    case 'CRITICAL': return '위험';
    case 'ALERT': return '경고';
    case 'WARNING': return '주의';
    default: return severity;
  }
};

export function useAnomalySse() {
  const eventSourceRef = useRef<EventSource | null>(null);
  const reconnectTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    const connect = () => {
      const baseUrl = import.meta.env.DEV ? '' : 'http://localhost:8080';
      const eventSource = new EventSource(`${baseUrl}/api/sse/subscribe`);
      eventSourceRef.current = eventSource;

      eventSource.addEventListener('anomaly-alert', (event) => {
        const alert: AnomalyAlert = JSON.parse(event.data);
        const detectedTime = format(new Date(alert.detectedAt), 'yyyy-MM-dd HH:mm:ss');

        toast({
          variant: alert.severity === 'CRITICAL' ? 'destructive' : 'default',
          title: `⚠️ ${alert.machineName} 이상 발생`,
          description: `발생 시간: ${detectedTime}\n심각도: ${getSeverityLabel(alert.severity)}`,
        });
      });

      eventSource.onerror = () => {
        console.error('SSE 연결 오류, 5초 후 재연결...');
        eventSource.close();
        reconnectTimeoutRef.current = setTimeout(connect, 5000);
      };
    };

    connect();

    return () => {
      eventSourceRef.current?.close();
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
    };
  }, []);
}
