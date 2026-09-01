'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname, useParams } from 'next/navigation';
import { cn } from '@/lib/utils';
import {
  LayoutDashboard,
  FolderGit2,
  GitBranch,
  FileText,
  Settings,
  User as UserIcon,
  ShieldAlert,
  Sparkles,
  ExternalLink,
  ChevronRight,
} from 'lucide-react';
import Badge from '@/components/ui/Badge';

export interface SidebarProps {
  isOpen?: boolean;
  onClose?: () => void;
}

interface NavItem {
  href: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  exact?: boolean;
  highlight?: boolean;
  badge?: string;
}

export const Sidebar: React.FC<SidebarProps> = ({ isOpen = false, onClose }) => {
  const pathname = usePathname();
  const params = useParams();
  const currentProjectId = (params?.projectId as string) || null;

  const navigation: NavItem[] = [
    { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, exact: true },
    { href: '/projects', label: 'Projects', icon: FolderGit2 },
    {
      href: currentProjectId ? `/projects/${currentProjectId}/releases` : '/projects',
      label: 'Releases',
      icon: GitBranch,
    },
    { href: '/reports', label: 'Reports', icon: FileText },
  ];

  const bottomNavigation: NavItem[] = [
    { href: '/settings', label: 'Settings', icon: Settings },
  ];


  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-slate-900/40 backdrop-blur-xs z-30 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar container */}
      <aside
        className={cn(
          'fixed lg:sticky top-16 z-30 flex flex-col justify-between w-64 h-[calc(100vh-4rem)] bg-white border-r border-slate-200 transition-transform duration-200 ease-in-out shrink-0 overflow-y-auto',
          isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        )}
      >
        <div className="p-4 space-y-6">
          {/* Active Workspace / Project Selector */}
          <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl space-y-1.5">
            <div className="flex items-center justify-between text-[11px] font-semibold text-slate-500 uppercase tracking-wider">
              <span>{currentProjectId ? 'Current Project' : 'Workspaces'}</span>
              <span className="text-indigo-600 font-mono text-[10px]">
                {currentProjectId ? 'ACTIVE' : 'ALL'}
              </span>
            </div>
            <Link
              href={currentProjectId ? `/projects/${currentProjectId}` : '/projects'}
              className="flex items-center justify-between group text-xs font-semibold text-slate-800 hover:text-indigo-600 transition"
            >
              <span className="truncate">
                {currentProjectId ? 'Project Workspace' : 'View All Projects'}
              </span>
              <ChevronRight className="w-3.5 h-3.5 text-slate-400 group-hover:translate-x-0.5 transition-transform" />
            </Link>
          </div>


          {/* Main Navigation */}
          <nav className="space-y-1">
            <p className="px-3 text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">
              Platform Navigation
            </p>
            {navigation.map((item) => {
              const isActive = item.exact
                ? pathname === item.href
                : pathname.startsWith(item.href);
              const Icon = item.icon;

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={onClose}
                  className={cn(
                    'flex items-center justify-between px-3 py-2 rounded-lg text-xs font-medium transition-colors',
                    isActive
                      ? 'bg-slate-900 text-white shadow-xs'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100',
                    item.highlight && !isActive && 'text-indigo-600 font-semibold bg-indigo-50/70 hover:bg-indigo-100/70'
                  )}
                >
                  <div className="flex items-center gap-2.5">
                    <Icon className={cn('w-4 h-4', isActive ? 'text-white' : item.highlight ? 'text-indigo-600' : 'text-slate-500')} />
                    <span>{item.label}</span>
                  </div>
                  {item.badge && (
                    <span className={cn('text-[10px] px-1.5 py-0.5 rounded-full font-bold', isActive ? 'bg-slate-700 text-white' : 'bg-slate-100 text-slate-600')}>
                      {item.badge}
                    </span>
                  )}
                </Link>
              );
            })}
          </nav>

          {/* Quick AI Readiness Overview Callout */}
          <div className="rounded-xl border border-indigo-100 bg-gradient-to-br from-indigo-50/80 to-sky-50/50 p-3.5 space-y-2">
            <div className="flex items-center gap-2 text-indigo-900 font-semibold text-xs">
              <Sparkles className="w-4 h-4 text-indigo-600" />
              <span>AI Readiness Score</span>
            </div>
            <p className="text-[11px] text-slate-600 leading-relaxed">
              Current release evaluation is ready for review with 1 blocker.
            </p>
            <Link
              href="/reports"
              className="inline-flex items-center gap-1 text-[11px] font-bold text-indigo-600 hover:text-indigo-700"
            >
              <span>View Evaluation</span>
              <ChevronRight className="w-3 h-3" />
            </Link>

          </div>
        </div>

        {/* Bottom Actions & User Profile */}
        <div className="p-4 border-t border-slate-200 space-y-2">
          {bottomNavigation.map((item) => {
            const isActive = pathname === item.href;
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={onClose}
                className={cn(
                  'flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-medium transition-colors',
                  isActive
                    ? 'bg-slate-900 text-white'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                )}
              >
                <Icon className={cn('w-4 h-4', isActive ? 'text-white' : 'text-slate-500')} />
                <span>{item.label}</span>
              </Link>
            );
          })}

          <div className="pt-2 text-[10px] text-slate-400 text-center">
            AI Release Readiness Analyzer v1.0
          </div>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
