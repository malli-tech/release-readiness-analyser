'use client';

import { useState, useEffect, useCallback } from 'react';
import { Project, CreateProjectRequest, UpdateProjectRequest } from '@/types/project';
import { apiClient, ApiError } from '@/lib/api';
import { useAuth } from './useAuth';

export function useProjects() {
  const { isAuthenticated } = useAuth();
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProjects = useCallback(async () => {
    if (!isAuthenticated) {
      setProjects([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const data = await apiClient.get<Project[]>('/api/projects');
      setProjects(data);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError('Failed to fetch projects. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    fetchProjects();
  }, [fetchProjects]);

  const getProject = async (id: string): Promise<Project> => {
    return await apiClient.get<Project>(`/api/projects/${id}`);
  };

  const createProject = async (data: CreateProjectRequest): Promise<Project> => {
    const newProject = await apiClient.post<Project>('/api/projects', data);
    setProjects((prev) => [newProject, ...prev]);
    return newProject;
  };

  const updateProject = async (id: string, data: UpdateProjectRequest): Promise<Project> => {
    const updated = await apiClient.put<Project>(`/api/projects/${id}`, data);
    setProjects((prev) => prev.map((p) => (p.id === id ? updated : p)));
    return updated;
  };

  const deleteProject = async (id: string): Promise<void> => {
    await apiClient.delete(`/api/projects/${id}`);
    setProjects((prev) => prev.filter((p) => p.id !== id));
  };

  return {
    projects,
    loading,
    error,
    fetchProjects,
    getProject,
    createProject,
    updateProject,
    deleteProject,
  };
}
