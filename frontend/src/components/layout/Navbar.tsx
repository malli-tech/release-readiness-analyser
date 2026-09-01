'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { fetchHealth, HealthResponse } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import {
  ShieldCheck,
  Menu,
  X,
  Bell,
  Search,
  CheckCircle2,
  AlertCircle,
  Activity,
  FolderGit2,
  LogOut,
  User as UserIcon,
} from 'lucide-react';
import Badge from '@/components/ui/Badge';

export interface NavbarProps {
  onToggleSidebar?: () => void;
  isSidebarOpen?: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({ onToggleSidebar, isSidebarOpen }) => {
  const pathname = usePathname();
  const { user, isAuthenticated, logout } = useAuth();
  const [backendHealth, setBackendHealth] = useState<HealthResponse | null>(null);
  const [healthLoading, setHealthLoading] = useState(true);

  useEffect(() => {
    fetchHealth()
      .then((data) => {
        setBackendHealth(data);
        setHealthLoading(false);
      })
      .catch(() => {
        setBackendHealth(null);
        setHealthLoading(false);
      });
  }, []);

  const isPublicPage = pathname === '/' || pathname === '/login' || pathname === '/register';

  return (
    <header className="sticky top-0 z-40 w-full bg-white/95 backdrop-blur border-b border-slate-200 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        {/* Left: Brand & Mobile Toggle */}
        <div className="flex items-center gap-3">
          {!isPublicPage && onToggleSidebar && (
            <button
              onClick={onToggleSidebar}
              className="lg:hidden p-2 rounded-lg text-slate-600 hover:bg-slate-100 transition"
              aria-label="Toggle Sidebar"
            >
              {isSidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          )}

          <Link href={isPublicPage ? '/' : '/dashboard'} className="flex items-center gap-2.5 group">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-600 to-indigo-800 flex items-center justify-center text-white shadow-sm shadow-indigo-200 group-hover:scale-105 transition-transform">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <span className="font-bold text-sm sm:text-base text-slate-900 tracking-tight block">
                AI Release Readiness
              </span>
              <span className="text-[10px] text-slate-500 font-medium tracking-wider uppercase hidden sm:block">
                Analyzer Platform
              </span>
            </div>
          </Link>
        </div>

        {/* Center: Search & Status Badge */}
        {!isPublicPage && (
          <div className="hidden md:flex items-center gap-3 max-w-xs w-full">
            <div className="relative w-full">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
              <input
                type="text"
                placeholder="Search releases, projects, findings..."
                className="w-full bg-slate-50 border border-slate-200 rounded-lg pl-9 pr-3 py-1.5 text-xs text-slate-700 placeholder-slate-400 focus:outline-none focus:ring-1 focus:ring-indigo-500 focus:bg-white transition"
              />
            </div>
          </div>
        )}

        {/* Right: Backend Health Status + User Info / Actions */}
        <div className="flex items-center gap-3">
          {/* Live Part 1 Backend Health Indicator */}
          <div className="hidden sm:flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-50 border border-slate-200 text-xs">
            <Activity className="w-3.5 h-3.5 text-slate-400" />
            <span className="text-[11px] text-slate-500 font-medium">Backend:</span>
            {healthLoading ? (
              <span className="w-2 h-2 rounded-full bg-amber-400 animate-pulse" title="Checking health..." />
            ) : backendHealth ? (
              <div className="flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-emerald-500" />
                <span className="text-[11px] font-semibold text-emerald-700">Connected</span>
              </div>
            ) : (
              <div className="flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-rose-500" />
                <span className="text-[11px] font-semibold text-rose-600">Disconnected</span>
              </div>
            )}
          </div>

          {!isAuthenticated ? (
            <div className="flex items-center gap-2">
              <Link
                href="/login"
                className="px-3.5 py-1.5 text-xs font-semibold text-slate-700 hover:text-slate-900 transition"
              >
                Log in
              </Link>
              <Link
                href="/register"
                className="px-3.5 py-1.5 text-xs font-semibold bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition shadow-sm"
              >
                Sign up free
              </Link>
            </div>
          ) : (
            <div className="flex items-center gap-2.5">
              <Link
                href="/projects/new"
                className="hidden md:inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-slate-900 hover:bg-slate-800 text-white rounded-lg transition shadow-xs"
              >
                <FolderGit2 className="w-3.5 h-3.5" />
                <span>New Project</span>
              </Link>

              <button
                aria-label="Notifications"
                className="p-2 rounded-lg text-slate-500 hover:text-slate-700 hover:bg-slate-100 transition relative"
              >
                <Bell className="w-4 h-4" />
                <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-indigo-600 ring-2 ring-white" />
              </button>

              {/* User Avatar & Logout */}
              <div className="flex items-center gap-2 pl-2 border-l border-slate-200">
                <Link
                  href="/settings"
                  className="flex items-center gap-2 hover:opacity-90 transition"
                >
                  <div className="w-8 h-8 rounded-full bg-indigo-100 border border-indigo-200 text-indigo-700 flex items-center justify-center font-bold text-xs">
                    {user?.name ? user.name.split(' ').map((n) => n[0]).join('') : 'U'}
                  </div>
                  <div className="hidden lg:block text-left">
                    <p className="text-xs font-semibold text-slate-900 leading-tight">{user?.name || 'User'}</p>
                    <p className="text-[10px] text-slate-500 leading-tight">{user?.role || 'STUDENT'}</p>
                  </div>
                </Link>

                <button
                  onClick={logout}
                  className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
                  title="Log out"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;
