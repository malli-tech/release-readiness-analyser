'use client';

import React from 'react';
import Link from 'next/link';
import { Project } from '@/types/project';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import { formatDate } from '@/lib/utils';
import { FolderGit2, Code2, ArrowRight, ExternalLink, Calendar, Layers } from 'lucide-react';

export interface ProjectCardProps {
  project: Project;
  onDelete?: (id: string) => void;
}

export const ProjectCard: React.FC<ProjectCardProps> = ({ project }) => {
  const projectType = project.projectType || project.type || 'WEB_APPLICATION';
  const primaryLanguage = project.primaryLanguage || project.language || 'Code';

  return (
    <Card className="hover:border-indigo-200 transition group flex flex-col justify-between">
      <div>
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between gap-2">
            <div className="flex items-center gap-2.5">
              <div className="p-2 rounded-lg bg-indigo-50 text-indigo-600 group-hover:bg-indigo-600 group-hover:text-white transition-colors">
                <FolderGit2 className="w-5 h-5" />
              </div>
              <div>
                <CardTitle className="text-sm font-bold text-slate-900 group-hover:text-indigo-600 transition-colors">
                  <Link href={`/projects/${project.id}`}>{project.name}</Link>
                </CardTitle>
                <div className="flex items-center gap-1.5 mt-0.5">
                  <span className="text-[10px] uppercase font-bold tracking-wider text-slate-400">
                    {projectType.replace('_', ' ')}
                  </span>
                </div>
              </div>
            </div>

            <Badge variant="neutral" size="sm">
              {primaryLanguage}
            </Badge>
          </div>
        </CardHeader>

        <CardContent className="space-y-3 pt-0">
          <p className="text-xs text-slate-500 line-clamp-2 min-h-[32px]">
            {project.description || 'No project description provided.'}
          </p>

          <div className="flex flex-wrap gap-1.5">
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-slate-100 text-slate-600 text-[10px] font-mono font-medium">
              <Code2 className="w-3 h-3 text-slate-400" />
              {primaryLanguage}
            </span>
            {project.framework && (
              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 text-[10px] font-mono font-medium">
                <Layers className="w-3 h-3 text-indigo-400" />
                {project.framework}
              </span>
            )}

            {project.repositoryUrl && (
              <a
                href={project.repositoryUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-slate-50 border border-slate-200 text-slate-600 text-[10px] font-mono hover:text-indigo-600 hover:border-indigo-300 transition"
              >
                <ExternalLink className="w-3 h-3" />
                Repo
              </a>
            )}
          </div>
        </CardContent>
      </div>

      <CardFooter className="justify-between border-t border-slate-100 pt-3 text-xs bg-slate-50/50">
        <div className="flex items-center gap-1 text-[11px] text-slate-400">
          <Calendar className="w-3.5 h-3.5" />
          <span>{formatDate(project.createdAt)}</span>
        </div>

        <Link href={`/projects/${project.id}`}>
          <Button variant="ghost" size="sm" rightIcon={<ArrowRight className="w-3.5 h-3.5" />}>
            View Project
          </Button>
        </Link>
      </CardFooter>
    </Card>
  );
};

export default ProjectCard;
