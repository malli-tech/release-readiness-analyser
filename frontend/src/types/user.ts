export interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  avatarUrl?: string;
  createdAt?: string;
  projectsCount?: number;
  institution?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
  message?: string;
}

export interface MessageResponse {
  message: string;
}

export interface ApiError {
  status: number;
  error: string;
  message: string;
  validationErrors?: Record<string, string>;
}
