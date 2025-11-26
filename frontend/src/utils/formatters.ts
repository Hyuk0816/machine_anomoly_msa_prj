import { format } from 'date-fns';

export const formatDateTime = (dateString: string): string => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd HH:mm:ss');
  } catch {
    return dateString;
  }
};

export const formatDate = (dateString: string): string => {
  try {
    return format(new Date(dateString), 'yyyy-MM-dd');
  } catch {
    return dateString;
  }
};

export const formatProbability = (probability: number): string => {
  return `${(probability * 100).toFixed(2)}%`;
};

export const getProbabilityColor = (probability: number): string => {
  if (probability > 0.7) return 'text-red-600 font-semibold';
  if (probability > 0.5) return 'text-orange-500 font-medium';
  return 'text-yellow-600';
};

export const getSeverityColor = (severity: string): string => {
  switch (severity) {
    case 'CRITICAL':
      return 'text-red-600 font-semibold';
    case 'ALERT':
      return 'text-orange-500 font-medium';
    case 'WARNING':
      return 'text-yellow-600 font-medium';
    default:
      return 'text-slate-400';
  }
};

export const getSeverityLabel = (severity: string): string => {
  switch (severity) {
    case 'CRITICAL':
      return '위험';
    case 'ALERT':
      return '주의';
    case 'WARNING':
      return '경고';
    default:
      return severity;
  }
};
