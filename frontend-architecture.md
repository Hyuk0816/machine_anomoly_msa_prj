# Smart Factory Predictive Maintenance - Frontend Architecture

## Executive Summary

Modern React-based frontend for real-time industrial IoT monitoring with TypeScript, performance-optimized data visualization, and type-safe API integration.

**Stack**: React 18 + Vite + TypeScript + TanStack Query + shadcn/ui + uPlot

**Key Characteristics**:
- Feature-based architecture for scalability
- Type-safe API layer with auto-generated types
- Real-time updates via intelligent polling
- Performance-optimized for large time-series datasets
- Accessible, responsive UI components

---

## Technology Stack

### Core Framework
| Technology | Version | Rationale |
|------------|---------|-----------|
| **React** | 18.3+ | Concurrent features, modern hooks, excellent ecosystem |
| **Vite** | 5.x | Fast HMR, optimized builds, native ESM, best-in-class DX |
| **TypeScript** | 5.x | Type safety, better refactoring, self-documenting code |
| **React Router** | 6.x | Declarative routing, data loading, nested routes |

### State Management
| Technology | Purpose | Rationale |
|------------|---------|-----------|
| **TanStack Query** | Server state | Automatic caching, refetching, mutations, polling - eliminates Redux boilerplate |
| **Zustand** | Client state | Lightweight (1KB), minimal boilerplate, excellent TypeScript support |
| **React Hook Form** | Form state | Performant, built-in validation, reduced re-renders |

**Decision**: Separate server state (TanStack Query) from client state (Zustand). TanStack Query handles all API interactions, caching, and synchronization. Zustand manages UI state like filters, preferences, and selections.

### UI Components
| Technology | Purpose | Rationale |
|------------|---------|-----------|
| **shadcn/ui** | Component library | Customizable, accessible (Radix UI), tree-shakeable, Tailwind-based, no runtime cost |
| **Radix UI** | Primitives | Unstyled, accessible components (WCAG AA), keyboard navigation built-in |
| **Tailwind CSS** | Styling | Utility-first, excellent DX, minimal CSS bundle, consistent design tokens |
| **Lucide React** | Icons | Modern, tree-shakeable, consistent style, 1000+ icons |

**Alternative Considered**: Material-UI (rejected - heavy bundle, outdated design), Ant Design (rejected - less customizable, large bundle)

### Data Visualization
| Technology | Purpose | Rationale |
|------------|---------|-----------|
| **uPlot** | Time-series charts | Extremely fast (60fps with 100K+ points), small bundle (45KB), designed for real-time data |
| **Recharts** | Simple charts | React-native API, good for dashboards with moderate data |

**Decision**: uPlot for sensor data visualization (performance critical), Recharts for dashboard overview charts (developer experience priority)

### API Layer
| Technology | Purpose | Rationale |
|------------|---------|-----------|
| **Axios** | HTTP client | Interceptors, request/response transformation, better error handling than fetch |
| **openapi-typescript** | Type generation | Auto-generate TypeScript types from OpenAPI spec, ensures frontend/backend type sync |
| **Zod** | Runtime validation | Type-safe runtime validation, catches API contract violations |

### Developer Tools
| Technology | Purpose |
|------------|---------|
| **ESLint** | Code linting with TypeScript rules |
| **Prettier** | Code formatting |
| **Husky** | Git hooks for pre-commit validation |
| **lint-staged** | Run linters on staged files only |

---

## Directory Structure

```
frontend/
├── public/                          # Static assets
│   ├── favicon.ico
│   └── robots.txt
│
├── src/
│   ├── app/                         # Application setup
│   │   ├── App.tsx                  # Root component
│   │   ├── router.tsx               # Route configuration
│   │   └── providers.tsx            # Global providers (Query, Theme)
│   │
│   ├── features/                    # Feature modules (domain-driven)
│   │   ├── dashboard/               # Real-time monitoring dashboard
│   │   │   ├── components/
│   │   │   │   ├── MachineStatusGrid.tsx
│   │   │   │   ├── LiveAnomalyFeed.tsx
│   │   │   │   ├── SystemHealthCard.tsx
│   │   │   │   └── QuickStatsPanel.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useDashboardData.ts       # Polling logic
│   │   │   │   └── useLiveAnomalies.ts
│   │   │   ├── pages/
│   │   │   │   └── DashboardPage.tsx
│   │   │   └── types/
│   │   │       └── dashboard.types.ts
│   │   │
│   │   ├── machines/                # Machine management
│   │   │   ├── components/
│   │   │   │   ├── MachineList.tsx           # Data table with filtering
│   │   │   │   ├── MachineCard.tsx           # Card view component
│   │   │   │   ├── MachineForm.tsx           # Create/Edit form
│   │   │   │   ├── MachineStatusBadge.tsx
│   │   │   │   └── MachineDeleteDialog.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useMachines.ts            # TanStack Query hooks
│   │   │   │   ├── useCreateMachine.ts
│   │   │   │   ├── useUpdateMachine.ts
│   │   │   │   ├── useDeleteMachine.ts
│   │   │   │   └── useMachineForm.ts         # Form logic
│   │   │   ├── pages/
│   │   │   │   ├── MachinesPage.tsx
│   │   │   │   └── MachineDetailPage.tsx
│   │   │   └── types/
│   │   │       └── machine.types.ts
│   │   │
│   │   ├── dcp-config/              # DCP configuration management
│   │   │   ├── components/
│   │   │   │   ├── DcpConfigList.tsx
│   │   │   │   ├── DcpConfigForm.tsx
│   │   │   │   └── DcpConfigCard.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useDcpConfigs.ts
│   │   │   │   ├── useCreateDcpConfig.ts
│   │   │   │   ├── useUpdateDcpConfig.ts
│   │   │   │   └── useDeleteDcpConfig.ts
│   │   │   ├── pages/
│   │   │   │   └── DcpConfigPage.tsx
│   │   │   └── types/
│   │   │       └── dcp-config.types.ts
│   │   │
│   │   ├── sensor-data/             # Sensor data visualization
│   │   │   ├── components/
│   │   │   │   ├── SensorChart.tsx           # uPlot wrapper
│   │   │   │   ├── TimeRangeSelector.tsx
│   │   │   │   ├── SensorMetricsTable.tsx
│   │   │   │   └── DataExportButton.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useSensorData.ts          # Time-range queries
│   │   │   │   └── useChartConfig.ts
│   │   │   ├── pages/
│   │   │   │   └── SensorDataPage.tsx
│   │   │   ├── types/
│   │   │   │   └── sensor-data.types.ts
│   │   │   └── utils/
│   │   │       ├── chartHelpers.ts
│   │   │       └── dataAggregation.ts
│   │   │
│   │   └── anomalies/               # Anomaly history
│   │       ├── components/
│   │       │   ├── AnomalyList.tsx
│   │       │   ├── AnomalyCard.tsx
│   │       │   ├── AnomalyFilters.tsx
│   │       │   └── AnomalySeverityBadge.tsx
│   │       ├── hooks/
│   │       │   ├── useAnomalies.ts
│   │       │   └── useAnomalySearch.ts
│   │       ├── pages/
│   │       │   └── AnomalyHistoryPage.tsx
│   │       └── types/
│   │           └── anomaly.types.ts
│   │
│   ├── shared/                      # Shared resources
│   │   ├── api/                     # API client layer
│   │   │   ├── client.ts            # Axios instance with interceptors
│   │   │   ├── queryClient.ts       # TanStack Query configuration
│   │   │   └── endpoints/
│   │   │       ├── machines.api.ts
│   │   │       ├── dcp-config.api.ts
│   │   │       ├── sensor-data.api.ts
│   │   │       └── anomalies.api.ts
│   │   │
│   │   ├── components/              # Shared UI components
│   │   │   ├── ui/                  # shadcn/ui components
│   │   │   │   ├── button.tsx
│   │   │   │   ├── dialog.tsx
│   │   │   │   ├── table.tsx
│   │   │   │   ├── card.tsx
│   │   │   │   ├── badge.tsx
│   │   │   │   ├── select.tsx
│   │   │   │   ├── input.tsx
│   │   │   │   └── ...
│   │   │   ├── layouts/
│   │   │   │   ├── RootLayout.tsx
│   │   │   │   ├── Sidebar.tsx
│   │   │   │   └── Header.tsx
│   │   │   ├── ErrorBoundary.tsx
│   │   │   ├── LoadingSpinner.tsx
│   │   │   ├── EmptyState.tsx
│   │   │   └── PageHeader.tsx
│   │   │
│   │   ├── hooks/                   # Shared custom hooks
│   │   │   ├── useDebounce.ts
│   │   │   ├── useMediaQuery.ts
│   │   │   ├── useLocalStorage.ts
│   │   │   └── useToast.ts
│   │   │
│   │   ├── store/                   # Zustand stores
│   │   │   ├── useUIStore.ts        # UI preferences, filters
│   │   │   └── useAuthStore.ts      # Auth state (future)
│   │   │
│   │   ├── types/                   # Shared TypeScript types
│   │   │   ├── api.types.ts         # Generated from OpenAPI
│   │   │   ├── common.types.ts
│   │   │   └── index.ts
│   │   │
│   │   └── utils/                   # Utility functions
│   │       ├── formatters.ts        # Date, number formatting
│   │       ├── validators.ts        # Validation helpers
│   │       ├── constants.ts
│   │       └── cn.ts                # Tailwind class merge utility
│   │
│   ├── lib/                         # Third-party configurations
│   │   ├── axios.config.ts
│   │   └── react-query.config.ts
│   │
│   ├── main.tsx                     # Application entry point
│   ├── index.css                    # Global styles + Tailwind imports
│   └── vite-env.d.ts                # Vite type definitions
│
├── .env.development                 # Development environment variables
├── .env.production                  # Production environment variables
├── .eslintrc.cjs                    # ESLint configuration
├── .prettierrc                      # Prettier configuration
├── components.json                  # shadcn/ui configuration
├── index.html                       # HTML entry point
├── package.json
├── postcss.config.js                # PostCSS configuration
├── tailwind.config.js               # Tailwind CSS configuration
├── tsconfig.json                    # TypeScript configuration
├── tsconfig.node.json               # TypeScript config for Node files
└── vite.config.ts                   # Vite configuration
```

---

## Architecture Patterns

### 1. Feature-Based Architecture

**Pattern**: Organize code by business domain (features) rather than technical layers.

**Structure**:
```
features/
  machines/           # All machine-related code
    components/       # Machine-specific components
    hooks/            # Machine-specific hooks
    pages/            # Machine pages
    types/            # Machine types
```

**Benefits**:
- High cohesion: Related code stays together
- Easy to locate functionality
- Clear feature boundaries
- Scalable for large teams
- Features can be independently tested/deployed

**Alternative Considered**: Layer-based architecture (components/, hooks/, pages/) - rejected due to poor scalability

---

### 2. API Client Layer

**Pattern**: Centralized, type-safe API client with endpoint abstraction.

**Implementation**:

```typescript
// shared/api/client.ts
import axios from 'axios'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor for auth tokens
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized
    }
    return Promise.reject(error)
  }
)
```

```typescript
// shared/api/endpoints/machines.api.ts
import { apiClient } from '../client'
import type { Machine, CreateMachineDTO, UpdateMachineDTO } from '@/shared/types/api.types'

export const machinesApi = {
  getAll: async (): Promise<Machine[]> => {
    const { data } = await apiClient.get<Machine[]>('/api/machine')
    return data
  },

  getById: async (id: number): Promise<Machine> => {
    const { data } = await apiClient.get<Machine>(`/api/machine/${id}`)
    return data
  },

  create: async (dto: CreateMachineDTO): Promise<Machine> => {
    const { data } = await apiClient.post<Machine>('/api/machine', dto)
    return data
  },

  update: async (id: number, dto: UpdateMachineDTO): Promise<Machine> => {
    const { data } = await apiClient.put<Machine>(`/api/machine/${id}`, dto)
    return data
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/api/machine/${id}`)
  }
}
```

```typescript
// features/machines/hooks/useMachines.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { machinesApi } from '@/shared/api/endpoints/machines.api'
import { toast } from '@/shared/hooks/useToast'

// Query keys for cache management
export const machineKeys = {
  all: ['machines'] as const,
  detail: (id: number) => ['machines', id] as const
}

export const useMachines = () => {
  return useQuery({
    queryKey: machineKeys.all,
    queryFn: machinesApi.getAll,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 3
  })
}

export const useMachine = (id: number) => {
  return useQuery({
    queryKey: machineKeys.detail(id),
    queryFn: () => machinesApi.getById(id),
    staleTime: 2 * 60 * 1000, // 2 minutes
    enabled: !!id
  })
}

export const useCreateMachine = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: machinesApi.create,
    onSuccess: (newMachine) => {
      // Invalidate and refetch machines list
      queryClient.invalidateQueries({ queryKey: machineKeys.all })
      toast({ title: 'Machine created successfully' })
    },
    onError: (error) => {
      toast({ title: 'Failed to create machine', variant: 'destructive' })
    }
  })
}

export const useUpdateMachine = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateMachineDTO }) =>
      machinesApi.update(id, data),
    onSuccess: (updatedMachine, { id }) => {
      // Update cache optimistically
      queryClient.setQueryData(machineKeys.detail(id), updatedMachine)
      queryClient.invalidateQueries({ queryKey: machineKeys.all })
      toast({ title: 'Machine updated successfully' })
    }
  })
}

export const useDeleteMachine = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: machinesApi.delete,
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: machineKeys.all })
      queryClient.removeQueries({ queryKey: machineKeys.detail(id) })
      toast({ title: 'Machine deleted successfully' })
    }
  })
}
```

**Benefits**:
- Type safety across API boundaries
- Centralized error handling
- Automatic request/response transformation
- Easy to mock for testing
- Single source of truth for API interactions

---

### 3. State Management Strategy

**Pattern**: Separation of concerns - server state (TanStack Query) vs client state (Zustand).

**Server State** (TanStack Query):
- All data from backend APIs
- Automatic caching and revalidation
- Optimistic updates
- Polling for real-time data

**Client State** (Zustand):
- UI preferences (theme, sidebar collapsed)
- Filter selections
- Form state (if not using React Hook Form)

```typescript
// shared/store/useUIStore.ts
import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface UIState {
  sidebarCollapsed: boolean
  theme: 'light' | 'dark'
  toggleSidebar: () => void
  setTheme: (theme: 'light' | 'dark') => void
}

export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      sidebarCollapsed: false,
      theme: 'light',
      toggleSidebar: () => set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),
      setTheme: (theme) => set({ theme })
    }),
    {
      name: 'ui-storage'
    }
  )
)
```

**Benefits**:
- Clear separation of concerns
- Minimal boilerplate
- Automatic persistence (Zustand persist)
- No prop drilling
- Easy to test

---

### 4. Real-Time Data Updates

**Pattern**: Intelligent polling with TanStack Query + future WebSocket support.

**Current Implementation** (Polling):

```typescript
// features/dashboard/hooks/useDashboardData.ts
import { useQuery } from '@tanstack/react-query'
import { machinesApi } from '@/shared/api/endpoints/machines.api'

export const useDashboardData = () => {
  return useQuery({
    queryKey: ['dashboard', 'machines'],
    queryFn: machinesApi.getAll,
    refetchInterval: 10000, // Poll every 10 seconds
    refetchIntervalInBackground: true,
    staleTime: 5000 // Consider data stale after 5 seconds
  })
}

export const useLiveAnomalies = () => {
  return useQuery({
    queryKey: ['dashboard', 'live-anomalies'],
    queryFn: anomaliesApi.getRecent,
    refetchInterval: 5000, // Poll every 5 seconds for critical data
    refetchIntervalInBackground: false // Pause when tab inactive
  })
}
```

**Future WebSocket Implementation**:

```typescript
// shared/api/websocket.ts (Future)
import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'

export const useWebSocketUpdates = () => {
  const queryClient = useQueryClient()

  useEffect(() => {
    const ws = new WebSocket('ws://localhost:8080/ws/updates')

    ws.onmessage = (event) => {
      const update = JSON.parse(event.data)

      switch (update.type) {
        case 'MACHINE_STATUS_UPDATE':
          queryClient.invalidateQueries({ queryKey: ['machines'] })
          break
        case 'NEW_ANOMALY':
          queryClient.invalidateQueries({ queryKey: ['anomalies'] })
          break
      }
    }

    return () => ws.close()
  }, [queryClient])
}
```

**Benefits**:
- No backend changes required initially
- TanStack Query handles polling efficiently
- Automatic pause when tab inactive
- Easy migration path to WebSocket
- Reduced server load with smart refetch intervals

---

### 5. Component Composition

**Pattern**: Compound components for complex UI with shared context.

**Example**: Machine Form Component

```typescript
// features/machines/components/MachineForm.tsx
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/shared/components/ui/button'
import { Input } from '@/shared/components/ui/input'
import { Form, FormField, FormItem, FormLabel, FormControl, FormMessage } from '@/shared/components/ui/form'

const machineFormSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  type: z.string().min(1, 'Type is required'),
  location: z.string().min(1, 'Location is required'),
  status: z.enum(['ACTIVE', 'INACTIVE', 'MAINTENANCE'])
})

type MachineFormData = z.infer<typeof machineFormSchema>

interface MachineFormProps {
  initialData?: MachineFormData
  onSubmit: (data: MachineFormData) => void
  isLoading?: boolean
}

export const MachineForm = ({ initialData, onSubmit, isLoading }: MachineFormProps) => {
  const form = useForm<MachineFormData>({
    resolver: zodResolver(machineFormSchema),
    defaultValues: initialData || {
      name: '',
      type: '',
      location: '',
      status: 'ACTIVE'
    }
  })

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
        <FormField
          control={form.control}
          name="name"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Machine Name</FormLabel>
              <FormControl>
                <Input placeholder="Enter machine name" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="type"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Machine Type</FormLabel>
              <FormControl>
                <Input placeholder="Enter machine type" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="location"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Location</FormLabel>
              <FormControl>
                <Input placeholder="Enter location" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" disabled={isLoading}>
          {isLoading ? 'Saving...' : 'Save Machine'}
        </Button>
      </form>
    </Form>
  )
}
```

**Usage in Page**:

```typescript
// features/machines/pages/MachinesPage.tsx
import { useState } from 'react'
import { MachineForm } from '../components/MachineForm'
import { MachineList } from '../components/MachineList'
import { useCreateMachine } from '../hooks/useMachines'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/shared/components/ui/dialog'
import { Button } from '@/shared/components/ui/button'

export const MachinesPage = () => {
  const [isFormOpen, setIsFormOpen] = useState(false)
  const createMachine = useCreateMachine()

  const handleCreate = (data: MachineFormData) => {
    createMachine.mutate(data, {
      onSuccess: () => setIsFormOpen(false)
    })
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">Machines</h1>
        <Button onClick={() => setIsFormOpen(true)}>Add Machine</Button>
      </div>

      <MachineList />

      <Dialog open={isFormOpen} onOpenChange={setIsFormOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create New Machine</DialogTitle>
          </DialogHeader>
          <MachineForm
            onSubmit={handleCreate}
            isLoading={createMachine.isPending}
          />
        </DialogContent>
      </Dialog>
    </div>
  )
}
```

**Benefits**:
- Reusable form component for create/edit
- Type-safe form validation with Zod
- Automatic error handling
- Accessible form components from shadcn/ui
- Clean separation of concerns

---

### 6. Data Visualization Pattern

**Pattern**: Specialized chart components with performance optimization.

**Sensor Data Chart** (uPlot):

```typescript
// features/sensor-data/components/SensorChart.tsx
import { useEffect, useRef } from 'react'
import uPlot from 'uplot'
import 'uplot/dist/uPlot.min.css'

interface SensorChartProps {
  data: {
    timestamps: number[]
    values: number[]
  }
  title: string
  yLabel: string
}

export const SensorChart = ({ data, title, yLabel }: SensorChartProps) => {
  const chartRef = useRef<HTMLDivElement>(null)
  const plotRef = useRef<uPlot>()

  useEffect(() => {
    if (!chartRef.current || !data.timestamps.length) return

    const opts: uPlot.Options = {
      width: chartRef.current.offsetWidth,
      height: 400,
      title,
      scales: {
        x: {
          time: true
        },
        y: {
          auto: true
        }
      },
      axes: [
        {
          label: 'Time'
        },
        {
          label: yLabel
        }
      ],
      series: [
        {},
        {
          label: yLabel,
          stroke: 'rgb(59, 130, 246)',
          width: 2,
          points: { show: false }
        }
      ]
    }

    const plotData = [data.timestamps, data.values]

    if (plotRef.current) {
      plotRef.current.setData(plotData)
    } else {
      plotRef.current = new uPlot(opts, plotData, chartRef.current)
    }

    // Handle resize
    const handleResize = () => {
      if (plotRef.current && chartRef.current) {
        plotRef.current.setSize({
          width: chartRef.current.offsetWidth,
          height: 400
        })
      }
    }

    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
      plotRef.current?.destroy()
    }
  }, [data, title, yLabel])

  return <div ref={chartRef} className="w-full" />
}
```

**Usage**:

```typescript
// features/sensor-data/pages/SensorDataPage.tsx
import { useState } from 'react'
import { SensorChart } from '../components/SensorChart'
import { useSensorData } from '../hooks/useSensorData'
import { TimeRangeSelector } from '../components/TimeRangeSelector'

export const SensorDataPage = () => {
  const [timeRange, setTimeRange] = useState({
    start: new Date(Date.now() - 24 * 60 * 60 * 1000), // Last 24 hours
    end: new Date()
  })

  const { data, isLoading } = useSensorData(machineId, timeRange)

  if (isLoading) return <LoadingSpinner />

  return (
    <div className="p-6 space-y-6">
      <TimeRangeSelector value={timeRange} onChange={setTimeRange} />

      <SensorChart
        data={{
          timestamps: data.map(d => d.timestamp),
          values: data.map(d => d.temperature)
        }}
        title="Temperature Over Time"
        yLabel="Temperature (°C)"
      />

      <SensorChart
        data={{
          timestamps: data.map(d => d.timestamp),
          values: data.map(d => d.vibration)
        }}
        title="Vibration Over Time"
        yLabel="Vibration (mm/s)"
      />
    </div>
  )
}
```

**Benefits**:
- 60fps performance with 100K+ data points
- Automatic resize handling
- Memory-efficient updates
- Small bundle size (45KB)
- Perfect for real-time sensor data

---

### 7. Routing Strategy

**Pattern**: Lazy-loaded routes with Suspense boundaries.

```typescript
// app/router.tsx
import { createBrowserRouter } from 'react-router-dom'
import { lazy, Suspense } from 'react'
import RootLayout from '@/shared/components/layouts/RootLayout'
import LoadingSpinner from '@/shared/components/LoadingSpinner'

// Lazy-loaded pages
const DashboardPage = lazy(() => import('@/features/dashboard/pages/DashboardPage'))
const MachinesPage = lazy(() => import('@/features/machines/pages/MachinesPage'))
const MachineDetailPage = lazy(() => import('@/features/machines/pages/MachineDetailPage'))
const DcpConfigPage = lazy(() => import('@/features/dcp-config/pages/DcpConfigPage'))
const SensorDataPage = lazy(() => import('@/features/sensor-data/pages/SensorDataPage'))
const AnomalyHistoryPage = lazy(() => import('@/features/anomalies/pages/AnomalyHistoryPage'))

// Wrapper for Suspense
const SuspenseWrapper = ({ children }: { children: React.ReactNode }) => (
  <Suspense fallback={<LoadingSpinner />}>{children}</Suspense>
)

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: (
          <SuspenseWrapper>
            <DashboardPage />
          </SuspenseWrapper>
        )
      },
      {
        path: 'machines',
        children: [
          {
            index: true,
            element: (
              <SuspenseWrapper>
                <MachinesPage />
              </SuspenseWrapper>
            )
          },
          {
            path: ':id',
            element: (
              <SuspenseWrapper>
                <MachineDetailPage />
              </SuspenseWrapper>
            )
          }
        ]
      },
      {
        path: 'dcp-config',
        element: (
          <SuspenseWrapper>
            <DcpConfigPage />
          </SuspenseWrapper>
        )
      },
      {
        path: 'sensor-data',
        element: (
          <SuspenseWrapper>
            <SensorDataPage />
          </SuspenseWrapper>
        )
      },
      {
        path: 'anomalies',
        element: (
          <SuspenseWrapper>
            <AnomalyHistoryPage />
          </SuspenseWrapper>
        )
      }
    ]
  }
])
```

```typescript
// app/App.tsx
import { RouterProvider } from 'react-router-dom'
import { router } from './router'

export default function App() {
  return <RouterProvider router={router} />
}
```

**Benefits**:
- Code splitting per route
- Faster initial load
- Better user experience with loading states
- Easy to add new routes
- Nested route support

---

## Performance Optimization Strategies

### 1. Code Splitting

**Vite Configuration**:

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          // Vendor splitting
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'query-vendor': ['@tanstack/react-query'],
          'ui-vendor': ['@radix-ui/react-dialog', '@radix-ui/react-select'],
          'chart-vendor': ['uplot', 'recharts']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  }
})
```

**Benefits**:
- Parallel chunk loading
- Better caching (vendor chunks change less)
- Smaller initial bundle
- Faster subsequent visits

---

### 2. Data Optimization

**Pagination**:

```typescript
// features/machines/hooks/useMachines.ts
import { useInfiniteQuery } from '@tanstack/react-query'

export const useMachinesPaginated = (pageSize = 20) => {
  return useInfiniteQuery({
    queryKey: ['machines', 'paginated'],
    queryFn: ({ pageParam = 0 }) =>
      machinesApi.getPage(pageParam, pageSize),
    getNextPageParam: (lastPage, pages) => {
      return lastPage.hasMore ? pages.length : undefined
    },
    staleTime: 5 * 60 * 1000
  })
}
```

**Virtual Scrolling** (for large lists):

```typescript
// features/sensor-data/components/VirtualSensorTable.tsx
import { useVirtualizer } from '@tanstack/react-virtual'
import { useRef } from 'react'

export const VirtualSensorTable = ({ data }: { data: SensorReading[] }) => {
  const parentRef = useRef<HTMLDivElement>(null)

  const virtualizer = useVirtualizer({
    count: data.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 50, // Row height
    overscan: 10
  })

  return (
    <div ref={parentRef} className="h-[600px] overflow-auto">
      <div
        style={{
          height: `${virtualizer.getTotalSize()}px`,
          position: 'relative'
        }}
      >
        {virtualizer.getVirtualItems().map((virtualRow) => {
          const reading = data[virtualRow.index]
          return (
            <div
              key={virtualRow.key}
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                width: '100%',
                height: `${virtualRow.size}px`,
                transform: `translateY(${virtualRow.start}px)`
              }}
            >
              <SensorRow data={reading} />
            </div>
          )
        })}
      </div>
    </div>
  )
}
```

**Benefits**:
- Render only visible rows
- Smooth scrolling with 10K+ items
- Reduced memory usage
- Better UX for large datasets

---

### 3. Caching Strategy

**TanStack Query Configuration**:

```typescript
// lib/react-query.config.ts
import { QueryClient } from '@tanstack/react-query'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Cache configuration
      staleTime: 5 * 60 * 1000, // 5 minutes default
      cacheTime: 10 * 60 * 1000, // 10 minutes
      retry: 3,
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),

      // Refetch configuration
      refetchOnWindowFocus: true,
      refetchOnReconnect: true,
      refetchOnMount: true
    },
    mutations: {
      retry: 1
    }
  }
})
```

**Feature-Specific Cache Times**:

```typescript
// Cache times by data volatility
const CACHE_TIMES = {
  // Static data
  machines: 5 * 60 * 1000,        // 5 minutes
  dcpConfigs: 5 * 60 * 1000,      // 5 minutes

  // Dynamic data
  sensorData: 30 * 1000,          // 30 seconds
  anomalies: 1 * 60 * 1000,       // 1 minute

  // Dashboard (real-time)
  dashboardStatus: 10 * 1000      // 10 seconds with polling
}
```

**Benefits**:
- Reduced API calls
- Faster navigation
- Better UX (instant data display)
- Lower server load

---

### 4. Rendering Optimization

**Memoization**:

```typescript
// features/machines/components/MachineCard.tsx
import { memo } from 'react'

interface MachineCardProps {
  machine: Machine
  onEdit: (id: number) => void
  onDelete: (id: number) => void
}

export const MachineCard = memo(({ machine, onEdit, onDelete }: MachineCardProps) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{machine.name}</CardTitle>
      </CardHeader>
      <CardContent>
        <p>Type: {machine.type}</p>
        <p>Location: {machine.location}</p>
        <MachineStatusBadge status={machine.status} />
      </CardContent>
      <CardFooter>
        <Button onClick={() => onEdit(machine.id)}>Edit</Button>
        <Button variant="destructive" onClick={() => onDelete(machine.id)}>
          Delete
        </Button>
      </CardFooter>
    </Card>
  )
}, (prevProps, nextProps) => {
  // Custom comparison - only re-render if machine data changed
  return prevProps.machine.id === nextProps.machine.id &&
         prevProps.machine.status === nextProps.machine.status
})
```

**Debounced Search**:

```typescript
// shared/hooks/useDebounce.ts
import { useEffect, useState } from 'react'

export const useDebounce = <T,>(value: T, delay = 500): T => {
  const [debouncedValue, setDebouncedValue] = useState<T>(value)

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value)
    }, delay)

    return () => clearTimeout(handler)
  }, [value, delay])

  return debouncedValue
}

// Usage in search component
const SearchInput = () => {
  const [search, setSearch] = useState('')
  const debouncedSearch = useDebounce(search, 300)

  const { data } = useMachines({ search: debouncedSearch })

  return <Input value={search} onChange={(e) => setSearch(e.target.value)} />
}
```

**Benefits**:
- Prevent unnecessary re-renders
- Reduce API calls during typing
- Better performance with large lists
- Smoother user experience

---

## Type Safety Implementation

### 1. OpenAPI Type Generation

**Setup**:

```bash
# Install dependencies
npm install -D openapi-typescript
```

**Generate types**:

```json
// package.json
{
  "scripts": {
    "generate-types": "openapi-typescript http://localhost:8080/v3/api-docs -o src/shared/types/api.types.ts"
  }
}
```

**Generated types** (example):

```typescript
// src/shared/types/api.types.ts (auto-generated)
export interface Machine {
  id: number
  name: string
  type: string
  location: string
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'
  createdAt: string
  updatedAt: string
}

export interface CreateMachineDTO {
  name: string
  type: string
  location: string
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'
}

export interface SensorData {
  id: number
  machineId: number
  temperature: number
  vibration: number
  pressure: number
  timestamp: string
}

export interface Anomaly {
  id: number
  machineId: number
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  description: string
  detectedAt: string
  resolved: boolean
}
```

**Benefits**:
- Single source of truth (backend OpenAPI spec)
- Automatic sync with backend changes
- Compile-time error detection
- Better IDE autocomplete

---

### 2. Runtime Validation with Zod

**API Response Validation**:

```typescript
// shared/api/schemas.ts
import { z } from 'zod'

export const MachineSchema = z.object({
  id: z.number(),
  name: z.string(),
  type: z.string(),
  location: z.string(),
  status: z.enum(['ACTIVE', 'INACTIVE', 'MAINTENANCE']),
  createdAt: z.string(),
  updatedAt: z.string()
})

export const MachinesArraySchema = z.array(MachineSchema)

// Usage in API client
import { MachinesArraySchema } from '@/shared/api/schemas'

export const machinesApi = {
  getAll: async (): Promise<Machine[]> => {
    const { data } = await apiClient.get('/api/machine')
    // Runtime validation
    return MachinesArraySchema.parse(data)
  }
}
```

**Benefits**:
- Catches API contract violations at runtime
- Type-safe parsing
- Automatic error messages
- Prevents corrupted data in app state

---

### 3. TypeScript Configuration

```json
// tsconfig.json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,

    /* Bundler mode */
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",

    /* Linting */
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "noImplicitAny": true,
    "strictNullChecks": true,

    /* Path aliases */
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

---

## Developer Experience

### 1. ESLint Configuration

```javascript
// .eslintrc.cjs
module.exports = {
  root: true,
  env: { browser: true, es2020: true },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
    'plugin:react/recommended',
    'plugin:react/jsx-runtime'
  ],
  ignorePatterns: ['dist', '.eslintrc.cjs'],
  parser: '@typescript-eslint/parser',
  plugins: ['react-refresh'],
  rules: {
    'react-refresh/only-export-components': [
      'warn',
      { allowConstantExport: true }
    ],
    '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
    'react/prop-types': 'off' // Using TypeScript
  },
  settings: {
    react: {
      version: 'detect'
    }
  }
}
```

---

### 2. Prettier Configuration

```json
// .prettierrc
{
  "semi": false,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "none",
  "printWidth": 100,
  "bracketSpacing": true,
  "arrowParens": "always"
}
```

---

### 3. Git Hooks

```json
// package.json
{
  "devDependencies": {
    "husky": "^8.0.0",
    "lint-staged": "^15.0.0"
  },
  "lint-staged": {
    "*.{ts,tsx}": [
      "eslint --fix",
      "prettier --write"
    ]
  }
}
```

```bash
# .husky/pre-commit
npm run lint-staged
```

---

### 4. Environment Variables

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
VITE_ENABLE_MOCK=false
```

```bash
# .env.production
VITE_API_BASE_URL=https://api.production.com
VITE_WS_URL=wss://api.production.com/ws
VITE_ENABLE_MOCK=false
```

```typescript
// Usage in code
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
```

---

## Accessibility (WCAG AA Compliance)

### 1. Semantic HTML

```typescript
// Always use semantic HTML elements
<nav> for navigation
<main> for main content
<article> for independent content
<section> for thematic grouping
<header> for headers
<footer> for footers
```

---

### 2. Keyboard Navigation

```typescript
// All interactive elements must be keyboard accessible
<Button onKeyDown={handleKeyDown}>Action</Button>

// Focus management in modals
useEffect(() => {
  if (isOpen) {
    dialogRef.current?.focus()
  }
}, [isOpen])
```

**shadcn/ui components** (Radix UI) provide keyboard navigation by default:
- Tab/Shift+Tab for focus navigation
- Enter/Space for activation
- Escape to close dialogs
- Arrow keys for select/combobox

---

### 3. ARIA Attributes

```typescript
// Proper ARIA labels for screen readers
<button aria-label="Close dialog" onClick={onClose}>
  <XIcon />
</button>

<input
  aria-label="Search machines"
  aria-describedby="search-help"
  type="text"
/>
<span id="search-help">Enter machine name or ID</span>
```

---

### 4. Color Contrast

```css
/* Ensure WCAG AA contrast ratios (4.5:1 for normal text, 3:1 for large text) */
/* Use Tailwind's default colors - they meet WCAG AA */

.text-primary /* Has sufficient contrast against bg-background */
.bg-destructive /* Has sufficient contrast for white text */
```

---

## Testing Strategy (Future Implementation)

### Unit Testing
- **Framework**: Vitest (Vite-native)
- **React Testing**: @testing-library/react
- **Coverage target**: >80%

### Integration Testing
- **Framework**: Playwright
- **E2E scenarios**: User journeys, form submissions

### Visual Testing
- **Framework**: Chromatic or Percy
- **Purpose**: Catch visual regressions

---

## Deployment Strategy

### Build Configuration

```json
// package.json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "format": "prettier --write \"src/**/*.{ts,tsx,css}\"",
    "type-check": "tsc --noEmit"
  }
}
```

### Docker Configuration

```dockerfile
# Dockerfile
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

```nginx
# nginx.conf
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy (if needed)
    location /api {
        proxy_pass http://portal-api:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

---

## Migration Path & Roadmap

### Phase 1: Foundation (Week 1-2)
- [ ] Initialize Vite + React + TypeScript project
- [ ] Install and configure dependencies
- [ ] Setup directory structure
- [ ] Configure ESLint, Prettier, Husky
- [ ] Create RootLayout and routing structure
- [ ] Setup TanStack Query and Zustand
- [ ] Create API client layer
- [ ] Generate types from OpenAPI spec

### Phase 2: Core Features (Week 3-4)
- [ ] Implement Machine Management (CRUD)
- [ ] Implement DCP Config Management (CRUD)
- [ ] Create shared UI components (forms, tables, dialogs)
- [ ] Add error handling and loading states

### Phase 3: Data Visualization (Week 5)
- [ ] Implement Sensor Data Viewer with uPlot
- [ ] Add time-range selection
- [ ] Implement Anomaly History viewer
- [ ] Add filtering and search

### Phase 4: Real-time Dashboard (Week 6)
- [ ] Build Dashboard layout
- [ ] Implement real-time polling
- [ ] Add status indicators
- [ ] Create live anomaly feed

### Phase 5: Polish & Optimization (Week 7)
- [ ] Performance optimization (memoization, virtual scrolling)
- [ ] Accessibility audit and fixes
- [ ] Responsive design refinement
- [ ] Add loading skeletons and empty states

### Phase 6: Future Enhancements
- [ ] WebSocket integration for true real-time updates
- [ ] Service Worker for offline capability
- [ ] Advanced filtering and sorting
- [ ] Data export functionality (CSV, Excel)
- [ ] User preferences and saved views
- [ ] Notifications system
- [ ] Dark mode
- [ ] Multi-language support (i18n)

---

## Key Abstractions Summary

### 1. API Client
- **Location**: `src/shared/api/`
- **Purpose**: Centralized, type-safe HTTP client
- **Pattern**: Axios with interceptors + endpoint abstraction

### 2. TanStack Query Hooks
- **Location**: `src/features/*/hooks/`
- **Purpose**: Server state management with caching
- **Pattern**: Custom hooks per entity (useMachines, useSensorData)

### 3. Zustand Stores
- **Location**: `src/shared/store/`
- **Purpose**: Client state (UI preferences, filters)
- **Pattern**: Slice-based stores with persist middleware

### 4. Form Handling
- **Location**: `src/features/*/components/`
- **Purpose**: Type-safe forms with validation
- **Pattern**: React Hook Form + Zod schemas

### 5. Chart Components
- **Location**: `src/features/sensor-data/components/`
- **Purpose**: High-performance data visualization
- **Pattern**: uPlot wrappers with hooks

### 6. Layout System
- **Location**: `src/shared/components/layouts/`
- **Purpose**: Consistent page structure
- **Pattern**: Nested layouts with outlet

---

## Conclusion

This architecture provides:

**Type Safety**: End-to-end TypeScript with generated types from backend OpenAPI spec

**Performance**: Code splitting, lazy loading, virtual scrolling, optimized caching, uPlot for real-time charts

**Developer Experience**: Feature-based structure, clear abstractions, minimal boilerplate, excellent tooling

**Scalability**: Clear boundaries, separation of concerns, easy to add features, team-friendly structure

**Accessibility**: WCAG AA compliance through Radix UI primitives, semantic HTML, keyboard navigation

**Maintainability**: Clean architecture, consistent patterns, self-documenting code, comprehensive type system

**Real-time Capabilities**: Intelligent polling with easy migration to WebSocket, optimized refetch strategies

The architecture is production-ready, scalable, and follows modern React best practices while being optimized for industrial IoT monitoring use cases.