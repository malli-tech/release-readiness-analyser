'use client';

import { useState, useEffect, useCallback } from 'react';
import { Release, CreateReleaseRequest, UpdateReleaseRequest } from '@/types/release';
import { apiClient, ApiError } from '@/lib/api';
import { useAuth } from './useAuth';

export function useReleases(projectId?: string) {
  const { isAuthenticated } = useAuth();
  const [releases, setReleases] = useState<Release[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchReleases = useCallback(async (targetProjectId?: string) => {
    const pid = targetProjectId || projectId;
    if (!isAuthenticated || !pid) {
      setReleases([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const data = await apiClient.get<Release[]>(`/api/projects/${pid}/releases`);
      setReleases(data);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError('Failed to fetch releases. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, projectId]);

  useEffect(() => {
    if (projectId) {
      fetchReleases(projectId);
    } else {
      setLoading(false);
    }
  }, [projectId, fetchReleases]);

  const getRelease = async (releaseId: string): Promise<Release> => {
    return await apiClient.get<Release>(`/api/releases/${releaseId}`);
  };

  const createRelease = async (targetProjectId: string, data: CreateReleaseRequest): Promise<Release> => {
    const newRelease = await apiClient.post<Release>(`/api/projects/${targetProjectId}/releases`, data);
    setReleases((prev) => [newRelease, ...prev]);
    return newRelease;
  };

  const updateRelease = async (releaseId: string, data: UpdateReleaseRequest): Promise<Release> => {
    const updated = await apiClient.put<Release>(`/api/releases/${releaseId}`, data);
    setReleases((prev) => prev.map((r) => (r.id === releaseId ? updated : r)));
    return updated;
  };

  const deleteRelease = async (releaseId: string): Promise<void> => {
    await apiClient.delete(`/api/releases/${releaseId}`);
    setReleases((prev) => prev.filter((r) => r.id !== releaseId));
  };

  return {
    releases,
    loading,
    error,
    fetchReleases,
    getRelease,
    createRelease,
    updateRelease,
    deleteRelease,
  };
}
