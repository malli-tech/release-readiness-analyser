export interface Project {
  id: string;
  userId?: string;
  name: string;
  description: string;
  projectType?: string;
  primaryLanguage?: string;
  type?: string;
  language?: string;
  framework?: string;
  repositoryUrl?: string;
  createdAt?: string;
  updatedAt?: string;
  lastAnalyzed?: string;
  releasesCount?: number;
  issuesCount?: number;
  latestVersion?: string;
  latestScore?: number;
  status?: 'READY' | 'NEEDS_REVIEW' | 'NOT_READY' | 'QUEUED';
  tags?: string[];
}


export interface CreateProjectRequest {
  name: string;
  description?: string;
  projectType: string;
  primaryLanguage: string;
  framework?: string;
  repositoryUrl?: string;
}

export interface UpdateProjectRequest {
  name: string;
  description?: string;
  projectType: string;
  primaryLanguage: string;
  framework?: string;
  repositoryUrl?: string;
}
