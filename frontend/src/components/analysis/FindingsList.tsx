'use client';

import React, { useState } from 'react';
import FindingCard from './FindingCard';
import FindingDetails from './FindingDetails';
import { Finding, FindingSeverity, FindingCategory } from '@/types/finding';
import { Search, Filter, AlertTriangle } from 'lucide-react';

export interface FindingsListProps {
  findings: Finding[];
}

export const FindingsList: React.FC<FindingsListProps> = ({ findings }) => {
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [search, setSearch] = useState('');
  const [activeFinding, setActiveFinding] = useState<Finding | null>(null);

  const filtered = findings.filter((f) => {
    const matchSev = severityFilter === 'ALL' || f.severity === severityFilter;
    const matchCat = categoryFilter === 'ALL' || f.category === categoryFilter;
    const matchSearch =
      f.title.toLowerCase().includes(search.toLowerCase()) ||
      f.filePath.toLowerCase().includes(search.toLowerCase()) ||
      f.description.toLowerCase().includes(search.toLowerCase());
    return matchSev && matchCat && matchSearch;
  });

  const categories = ['ALL', 'Security', 'Testing', 'Code Quality', 'Dependencies', 'Performance'];
  const severities = ['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];

  return (
    <div className="space-y-4">
      {/* Search and Filters Bar */}
      <div className="flex flex-col md:flex-row items-center justify-between gap-3 bg-white p-3 rounded-xl border border-slate-200">
        <div className="relative w-full md:max-w-xs">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search findings or files..."
            className="w-full bg-slate-50 border border-slate-200 rounded-lg pl-9 pr-3 py-1.5 text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </div>

        <div className="flex flex-wrap items-center gap-2 w-full md:w-auto">
          {/* Severity Tabs */}
          <div className="flex items-center gap-1 bg-slate-100 p-1 rounded-lg text-xs">
            {severities.map((sev) => (
              <button
                key={sev}
                onClick={() => setSeverityFilter(sev)}
                className={`px-2 py-0.5 rounded font-medium transition ${
                  severityFilter === sev
                    ? 'bg-white text-slate-900 font-bold shadow-xs'
                    : 'text-slate-500 hover:text-slate-800'
                }`}
              >
                {sev}
              </button>
            ))}
          </div>

          {/* Category Dropdown */}
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1 text-xs text-slate-700 focus:outline-none"
          >
            {categories.map((cat) => (
              <option key={cat} value={cat}>{cat}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Findings Cards */}
      {filtered.length === 0 ? (
        <div className="p-8 text-center bg-white rounded-xl border border-slate-200 space-y-2">
          <AlertTriangle className="w-8 h-8 text-slate-300 mx-auto" />
          <p className="text-xs font-semibold text-slate-700">No findings match your filter criteria.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map((f) => (
            <FindingCard key={f.id} finding={f} onViewDetails={setActiveFinding} />
          ))}
        </div>
      )}

      {/* Finding Details Modal */}
      {activeFinding && (
        <FindingDetails finding={activeFinding} onClose={() => setActiveFinding(null)} />
      )}
    </div>
  );
};

export default FindingsList;
