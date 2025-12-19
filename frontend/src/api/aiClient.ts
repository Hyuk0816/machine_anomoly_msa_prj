import axios from 'axios';

const AI_SERVER_URL = import.meta.env.VITE_AI_SERVER_URL || 'http://localhost:8000';

export const aiClient = axios.create({
  baseURL: import.meta.env.DEV ? '/ai-api' : AI_SERVER_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

aiClient.interceptors.request.use(
  (config) => {
    console.log(`AI API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

aiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('AI API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);