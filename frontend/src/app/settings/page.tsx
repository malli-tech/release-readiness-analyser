'use client';

import React, { useState } from 'react';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import { useAuth } from '@/hooks/useAuth';
import { User, Shield, Sliders, LogOut, Save, CheckCircle2 } from 'lucide-react';

export default function SettingsPage() {
  const { user, logout } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [name, setName] = useState(user?.name || '');
  const [email, setEmail] = useState(user?.email || '');
  const [autoScan, setAutoScan] = useState(true);
  const [saved, setSaved] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            <div>
              <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                Settings & Preferences
              </h1>
              <p className="text-xs sm:text-sm text-slate-500 mt-1">
                Manage your profile, authentication credentials, analysis thresholds, and account security.
              </p>
            </div>

            <form onSubmit={handleSave} className="max-w-3xl space-y-6">
              {/* Profile Section */}
              <Card>
                <CardHeader>
                  <div className="flex items-center gap-2">
                    <User className="w-4 h-4 text-indigo-600" />
                    <CardTitle className="text-sm">Developer Profile</CardTitle>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div className="space-y-1.5">
                      <label className="block text-xs font-semibold text-slate-700">Full Name</label>
                      <input
                        type="text"
                        value={name || user?.name || ''}
                        onChange={(e) => setName(e.target.value)}
                        className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                      />
                    </div>
                    <div className="space-y-1.5">
                      <label className="block text-xs font-semibold text-slate-700">Email Address</label>
                      <input
                        type="email"
                        disabled
                        value={user?.email || ''}
                        className="w-full px-3 py-2 text-xs rounded-lg border border-slate-200 bg-slate-100 text-slate-600"
                      />
                    </div>
                  </div>

                  <div className="pt-2 flex items-center gap-2 text-xs text-slate-500">
                    <span className="font-semibold text-slate-700">Role:</span>
                    <span className="font-mono bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded font-bold">
                      {user?.role || 'STUDENT'}
                    </span>
                  </div>
                </CardContent>
              </Card>

              {/* Analysis Preferences */}
              <Card>
                <CardHeader>
                  <div className="flex items-center gap-2">
                    <Sliders className="w-4 h-4 text-indigo-600" />
                    <CardTitle className="text-sm">Analysis Preferences</CardTitle>
                  </div>
                </CardHeader>
                <CardContent className="space-y-3 text-xs">
                  <label className="flex items-center gap-3 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={autoScan}
                      onChange={(e) => setAutoScan(e.target.checked)}
                      className="w-4 h-4 text-indigo-600 rounded"
                    />
                    <span className="font-medium text-slate-700">
                      Auto-trigger dependency and vulnerability check on ZIP upload
                    </span>
                  </label>
                  <label className="flex items-center gap-3 cursor-pointer">
                    <input
                      type="checkbox"
                      defaultChecked
                      className="w-4 h-4 text-indigo-600 rounded"
                    />
                    <span className="font-medium text-slate-700">
                      Generate plain-language AI explanation for all Critical and High severity findings
                    </span>
                  </label>
                </CardContent>
                <CardFooter className="justify-between">
                  <span className="text-xs text-emerald-600 font-semibold">{saved ? 'Saved changes!' : ''}</span>
                  <Button type="submit" size="sm" leftIcon={<Save className="w-3.5 h-3.5" />}>
                    Save Settings
                  </Button>
                </CardFooter>
              </Card>

              {/* Account Security & Logout */}
              <Card className="border-rose-200 bg-rose-50/20">
                <CardHeader>
                  <div className="flex items-center gap-2">
                    <Shield className="w-4 h-4 text-rose-600" />
                    <CardTitle className="text-sm text-rose-900">Account Session</CardTitle>
                  </div>
                </CardHeader>
                <CardContent className="flex items-center justify-between">
                  <div>
                    <p className="text-xs font-semibold text-slate-800">Sign out of current session</p>
                    <p className="text-[11px] text-slate-500">Clears JWT security token from client storage.</p>
                  </div>
                  <Button
                    type="button"
                    variant="danger"
                    size="sm"
                    onClick={logout}
                    leftIcon={<LogOut className="w-3.5 h-3.5" />}
                  >
                    Log Out
                  </Button>
                </CardContent>
              </Card>
            </form>
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
