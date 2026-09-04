'use client';

import { useState, useCallback } from 'react';
import { apiClient } from '@/lib/api';
import { AnalysisResponse } from '@/types/analysis';

export function useAnalysis() {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [analysis, setAnalysis] = useState<AnalysisResponse | null>(null);

  const startAnalysis = useCallback(async (releaseId: string): Promise<AnalysisResponse> => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiClient.post<AnalysisResponse>(`/api/releases/${releaseId}/analysis`);
      setAnalysis(data);
      return data;
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to start project detection and analysis.';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const getLatestAnalysis = useCallback(async (releaseId: string): Promise<AnalysisResponse> => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiClient.get<AnalysisResponse>(`/api/releases/${releaseId}/analysis`);
      setAnalysis(data);
      return data;
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to retrieve analysis profile.';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const getAnalysis = useCallback(async (analysisId: string): Promise<AnalysisResponse> => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiClient.get<AnalysisResponse>(`/api/analyses/${analysisId}`);
      setAnalysis(data);
      return data;
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to retrieve analysis details.';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    loading,
    error,
    analysis,
    startAnalysis,
    getLatestAnalysis,
    getAnalysis,
  };
}
