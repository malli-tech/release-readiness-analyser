'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { useProjects } from '@/hooks/useProjects';
import ProjectCard from './ProjectCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { Plus, Search, FolderGit2, AlertCircle, RefreshCw } from 'lucide-react';

export const ProjectList: React.FC = () => {
  const { projects, loading, error, fetchProjects, deleteProject } = useProjects();
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');

  const filteredProjects = projects.filter((p) => {
    const lang = p.primaryLanguage || p.language || '';
    const type = p.projectType || p.type || '';
    const framework = p.framework || '';
    const desc = p.description || '';

    const matchesSearch =
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      desc.toLowerCase().includes(searchTerm.toLowerCase()) ||
      lang.toLowerCase().includes(searchTerm.toLowerCase()) ||
      framework.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesType = typeFilter === 'ALL' || type === typeFilter;
    return matchesSearch && matchesType;
  });



  return (
    <div className="space-y-6">
      {/* Controls Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex flex-col sm:flex-row sm:items-center gap-3 w-full sm:w-auto">
          {/* Search */}
          <div className="relative w-full sm:w-72">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search projects..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full bg-white border border-slate-200 rounded-lg pl-9 pr-3 py-1.5 text-xs text-slate-700 placeholder-slate-400 focus:outline-none focus:ring-1 focus:ring-indigo-500 transition"
            />
          </div>

          {/* Type Filter */}
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="bg-white border border-slate-200 rounded-lg px-3 py-1.5 text-xs text-slate-700 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          >
            <option value="ALL">All Project Types</option>
            <option value="WEB_APPLICATION">Web Application</option>
            <option value="REST_API">REST API</option>
            <option value="MICROSERVICE">Microservice</option>
            <option value="CLI_TOOL">CLI Tool</option>
            <option value="MOBILE_BACKEND">Mobile Backend</option>
            <option value="LIBRARY">Library / Package</option>
          </select>
        </div>

        <Link href="/projects/new">
          <Button size="sm" leftIcon={<Plus className="w-4 h-4" />}>
            Create Project
          </Button>
        </Link>
      </div>

      {/* Content State */}
      {loading ? (
        <div className="py-16 flex flex-col items-center justify-center space-y-3 bg-white rounded-xl border border-slate-200">
          <Spinner size="lg" label="Loading projects..." />
        </div>
      ) : error ? (
        <div className="p-8 text-center bg-rose-50 rounded-xl border border-rose-200 space-y-3">
          <div className="w-10 h-10 rounded-full bg-rose-100 text-rose-600 flex items-center justify-center mx-auto">
            <AlertCircle className="w-5 h-5" />
          </div>
          <p className="text-xs font-semibold text-rose-800">{error}</p>
          <Button
            size="sm"
            variant="outline"
            onClick={fetchProjects}
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
          >
            Retry
          </Button>
        </div>
      ) : projects.length === 0 ? (
        /* Empty State */
        <div className="py-16 text-center bg-white rounded-xl border border-dashed border-slate-300 space-y-4 max-w-lg mx-auto p-6">
          <div className="w-12 h-12 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center mx-auto">
            <FolderGit2 className="w-6 h-6" />
          </div>
          <div className="space-y-1">
            <h3 className="text-sm font-bold text-slate-900">No projects yet</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Get started by creating your first project to perform automated release audits and code readiness scoring.
            </p>
          </div>
          <Link href="/projects/new">
            <Button size="sm" leftIcon={<Plus className="w-4 h-4" />}>
              Create your first project
            </Button>
          </Link>
        </div>
      ) : filteredProjects.length === 0 ? (
        <div className="py-12 text-center bg-white rounded-xl border border-slate-200 text-slate-500 text-xs">
          No projects matching &quot;{searchTerm}&quot;.
        </div>
      ) : (
        /* Project Grid */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredProjects.map((project) => (
            <ProjectCard
              key={project.id}
              project={project}
              onDelete={deleteProject}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default ProjectList;
