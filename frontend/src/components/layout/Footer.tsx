import React from 'react';
import Link from 'next/link';
import { ShieldCheck } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="border-t border-slate-200 bg-white text-slate-600 text-xs py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-md bg-indigo-600 flex items-center justify-center text-white">
            <ShieldCheck className="w-3.5 h-3.5" />
          </div>
          <span className="font-semibold text-slate-800">AI Release Readiness Analyzer</span>
          <span className="text-slate-400">© 2026</span>
        </div>

        <div className="flex items-center gap-6 text-slate-500">
          <Link href="/dashboard" className="hover:text-slate-900 transition">
            Dashboard
          </Link>
          <Link href="/projects" className="hover:text-slate-900 transition">
            Projects
          </Link>
          <Link href="/reports" className="hover:text-slate-900 transition">
            Reports
          </Link>
          <Link href="/settings" className="hover:text-slate-900 transition">
            Settings
          </Link>
        </div>

        <div className="text-slate-400 text-[11px]">
          Engineering Capstone Platform • 5-Member Team Monorepo
        </div>
      </div>
    </footer>
  );
};

export default Footer;
