import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function getScoreColor(score: number): {
  text: string;
  bg: string;
  border: string;
  badge: 'ready' | 'review' | 'critical';
  label: string;
} {
  if (score >= 90) {
    return {
      text: 'text-emerald-600',
      bg: 'bg-emerald-500',
      border: 'border-emerald-500',
      badge: 'ready',
      label: 'READY',
    };
  }
  if (score >= 70) {
    return {
      text: 'text-amber-600',
      bg: 'bg-amber-500',
      border: 'border-amber-500',
      badge: 'review',
      label: 'NEEDS REVIEW',
    };
  }
  return {
    text: 'text-rose-600',
    bg: 'bg-rose-500',
    border: 'border-rose-500',
    badge: 'critical',
    label: 'NOT READY',
  };
}

export function getSeverityBadgeVariant(severity: string): 'critical' | 'warning' | 'review' | 'info' {
  switch (severity.toUpperCase()) {
    case 'CRITICAL':
      return 'critical';
    case 'HIGH':
      return 'warning';
    case 'MEDIUM':
      return 'review';
    case 'LOW':
    default:
      return 'info';
  }
}

export function formatDate(dateString?: string): string {
  if (!dateString) return 'N/A';
  try {
    const d = new Date(dateString);
    if (isNaN(d.getTime())) return dateString;
    return d.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  } catch {
    return dateString;
  }
}

export function formatBytes(bytes: number, decimals = 2): string {
  if (!bytes || bytes === 0) return '0 Bytes';
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
}
