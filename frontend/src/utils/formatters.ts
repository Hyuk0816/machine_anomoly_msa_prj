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
