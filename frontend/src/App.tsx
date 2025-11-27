import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from './contexts/ThemeContext';
import { Layout } from './components/layout/Layout';
import { Dashboard } from './features/dashboard/Dashboard';
import { MachineList } from './features/machines/MachineList';
import { DcpConfigList } from './features/dcp-config/DcpConfigList';
import { SensorDataViewer } from './features/sensor-data/SensorDataViewer';
import { AnomalyList } from './features/anomalies/AnomalyList';
import { Toaster } from './components/ui/toaster';
import { useAnomalySse } from './hooks/useAnomalySse';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 30000,
    },
  },
});

function SseProvider({ children }: { children: React.ReactNode }) {
  useAnomalySse();
  return <>{children}</>;
}

function App() {
  return (
    <ThemeProvider>
      <QueryClientProvider client={queryClient}>
        <SseProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/" element={<Layout />}>
                <Route index element={<Dashboard />} />
                <Route path="machines" element={<MachineList />} />
                <Route path="dcp-configs" element={<DcpConfigList />} />
                <Route path="sensor-data" element={<SensorDataViewer />} />
                <Route path="anomalies" element={<AnomalyList />} />
              </Route>
            </Routes>
          </BrowserRouter>
          <Toaster />
        </SseProvider>
      </QueryClientProvider>
    </ThemeProvider>
  );
}

export default App;
