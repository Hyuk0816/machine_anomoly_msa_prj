// Machine types
export interface MachineCreateDto {
  name: string;
  type: string;
}

export interface MachineModifyDto {
  name?: string;
  type?: string;
}

export interface MachineResponseDto {
  id: number;
  name: string;
  type: string;
}

// DCP Config types
export interface DcpConfigCreateDto {
  machineId: number;
  collectInterval: number;
  apiEndpoint: string;
}

export interface DcpConfigModifyDto {
  machineId: number;
  collectInterval: number;
  apiEndpoint: string;
}

export interface DcpConfigResponseDto {
  id: number;
  machineId: number;
  collectInterval: number;
  apiEndpoint: string;
}

// Sensor Data types
export interface MachineSensorDataResponseDto {
  id: number;
  machineId: number;
  airTemperature: number;
  processTemperature: number;
  rotationalSpeed: number;
  torque: number;
  toolWear: number;
  createdAt: string;
}

// Anomaly History types
export interface EventMessageSensorData {
  airTemperature: number;
  processTemperature: number;
  rotationalSpeed: number;
  torque: number;
  toolWear: number;
}

export interface AnomalyHistoryResponseDto {
  id: number;
  machineId: number;
  detectedAt: string;
  anomalyProbability: number;
  eventMessageSensorData: EventMessageSensorData;
  severity: Severity;
}

// Machine types enum
export const MACHINE_TYPES = [
    'LOW',
    'MEDIUM',
    'HIGH',
] as const;

export const SEVERITY = ['WARNING','ALERT','CRITICAL'] as const;
export type Severity = typeof SEVERITY[number];
