'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { RecommendationRecord, RecommendationPriority, RecommendationStatus } from '@/types/recommendation';
import { apiClient } from '@/lib/api';
import {
  CheckSquare,
  Filter,
  AlertCircle,
  RefreshCw,
  FileCode,
  ArrowRight,
  ShieldAlert,
  Zap,
  CheckCircle2,
  Clock,
  Layers,
} from 'lucide-react';

export interface RecommendationsSectionProps {
  analysisId?: string;
  recommendations?: RecommendationRecord[];
  onRecommendationSelect?: (recommendation: RecommendationRecord) => void;
}

export const RecommendationsSection: React.FC<RecommendationsSectionProps> = ({
  analysisId,
  recommendations: initialRecommendations,
  onRecommendationSelect,
}) => {
  const [recommendations, setRecommendations] = useState<RecommendationRecord[]>(initialRecommendations || []);
  const [loading, setLoading] = useState<boolean>(!initialRecommendations && !!analysisId);
  const [error, setError] = useState<string | null>(null);

  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const fetchRecommendations = useCallback(async () => {
    if (!analysisId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await apiClient.get<RecommendationRecord[]>(`/api/analyses/${analysisId}/recommendations`);
      setRecommendations(data || []);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to fetch recommendations for this analysis.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [analysisId]);

  useEffect(() => {
    if (initialRecommendations) {
      setRecommendations(initialRecommendations);
    } else if (analysisId) {
      fetchRecommendations();
    }
  }, [analysisId, initialRecommendations, fetchRecommendations]);

  const filteredRecommendations = recommendations.filter((rec) => {
    const matchesPriority = priorityFilter === 'ALL' || rec.priority === priorityFilter;
    const matchesCategory =
      categoryFilter === 'ALL' ||
      rec.category === categoryFilter ||
      (categoryFilter === 'DEPENDENCY' && (rec.category === 'DEPENDENCIES' || rec.category === 'DEPENDENCY'));
    const matchesStatus = statusFilter === 'ALL' || rec.status === statusFilter;
    return matchesPriority && matchesCategory && matchesStatus;
  });

  const getPriorityBadgeVariant = (priority: RecommendationPriority) => {
    switch (priority) {
      case 'CRITICAL':
        return 'critical';
      case 'HIGH':
        return 'warning';
      case 'MEDIUM':
        return 'info';
      case 'LOW':
      default:
        return 'neutral';
    }
  };

  const getStatusBadgeVariant = (status: RecommendationStatus) => {
    switch (status) {
      case 'RESOLVED':
        return 'ready';
      case 'IN_PROGRESS':
        return 'review';
      case 'OPEN':
        return 'info';
      case 'IGNORED':
      default:
        return 'neutral';
    }
  };

  return (
    <Card className="border-indigo-100 bg-white shadow-xs">
      <CardHeader className="pb-3 border-b border-slate-100">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-indigo-50 text-indigo-600 border border-indigo-100">
              <CheckSquare className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <CardTitle className="text-base font-bold text-slate-900">Deterministic Recommendations</CardTitle>
                <Badge variant="info" size="sm">
                  {filteredRecommendations.length} {filteredRecommendations.length === 1 ? 'Action' : 'Actions'}
                </Badge>
              </div>
              <p className="text-xs text-slate-500">
                1-to-1 finding traceability, remediation actions, priority ranking, and effort estimates.
              </p>
            </div>
          </div>

          {/* Controls & Filters */}
          <div className="flex flex-wrap items-center gap-2 text-xs">
            {/* Priority Filter */}
            <div className="flex items-center gap-1 bg-slate-100 p-1 rounded-lg">
              <span className="text-[10px] text-slate-400 font-semibold px-1 hidden sm:inline">PRIORITY:</span>
              {['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map((p) => (
                <button
                  key={p}
                  type="button"
                  onClick={() => setPriorityFilter(p)}
                  aria-label={`Filter by priority ${p}`}
                  className={`px-2 py-0.5 rounded-md font-semibold text-[11px] transition ${
                    priorityFilter === p ? 'bg-white text-indigo-900 shadow-2xs font-bold' : 'text-slate-500 hover:text-slate-800'
                  }`}
                >
                  {p}
                </button>
              ))}
            </div>

            {/* Category Filter */}
            <div className="flex items-center gap-1 bg-slate-100 p-1 rounded-lg">
              <span className="text-[10px] text-slate-400 font-semibold px-1 hidden sm:inline">CATEGORY:</span>
              {['ALL', 'SECURITY', 'CODE_QUALITY', 'TESTING', 'DEPENDENCY', 'PERFORMANCE'].map((cat) => (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setCategoryFilter(cat)}
                  aria-label={`Filter by category ${cat}`}
                  className={`px-2 py-0.5 rounded-md font-semibold text-[11px] transition ${
                    categoryFilter === cat ? 'bg-white text-indigo-900 shadow-2xs font-bold' : 'text-slate-500 hover:text-slate-800'
                  }`}
                >
                  {cat === 'ALL' ? 'All' : cat === 'CODE_QUALITY' ? 'Quality' : cat === 'DEPENDENCY' ? 'Deps' : cat}
                </button>
              ))}
            </div>

            {/* Status Filter */}
            <div className="flex items-center gap-1 bg-slate-100 p-1 rounded-lg">
              <span className="text-[10px] text-slate-400 font-semibold px-1 hidden sm:inline">STATUS:</span>
              {['ALL', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'IGNORED'].map((st) => (
                <button
                  key={st}
                  type="button"
                  onClick={() => setStatusFilter(st)}
                  aria-label={`Filter by status ${st}`}
                  className={`px-2 py-0.5 rounded-md font-semibold text-[11px] transition ${
                    statusFilter === st ? 'bg-white text-indigo-900 shadow-2xs font-bold' : 'text-slate-500 hover:text-slate-800'
                  }`}
                >
                  {st === 'IN_PROGRESS' ? 'Progress' : st}
                </button>
              ))}
            </div>
          </div>
        </div>
      </CardHeader>

      <CardContent className="pt-4 space-y-4">
        {/* Loading State */}
        {loading ? (
          <div className="py-12 flex flex-col items-center justify-center space-y-3 bg-slate-50 rounded-xl border border-slate-200">
            <Spinner size="md" label="Retrieving analysis recommendations..." />
          </div>
        ) : error ? (
          /* API Error State */
          <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 space-y-2 text-xs">
            <div className="flex items-center gap-2 font-bold text-rose-900">
              <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
              <span>Recommendations Unavailable</span>
            </div>
            <p className="text-rose-700 leading-relaxed">{error}</p>
            {analysisId && (
              <div className="pt-1">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={fetchRecommendations}
                  leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
                >
                  Retry Loading Recommendations
                </Button>
              </div>
            )}
          </div>
        ) : filteredRecommendations.length === 0 ? (
          /* Empty State */
          <div className="py-10 text-center space-y-2 bg-slate-50/50 rounded-xl border border-slate-200">
            <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto" />
            <h4 className="text-xs font-bold text-slate-800">No Recommendations Found</h4>
            <p className="text-[11px] text-slate-500 max-w-sm mx-auto">
              {recommendations.length > 0
                ? 'No recommendations match the selected filter criteria.'
                : 'No static findings requiring remediation were detected for this analysis.'}
            </p>
          </div>
        ) : (
          /* Recommendations List */
          filteredRecommendations.map((rec) => (
            <div
              key={rec.id || rec.findingId}
              onClick={() => onRecommendationSelect && onRecommendationSelect(rec)}
              className="p-4 rounded-xl border border-slate-200 bg-white hover:border-indigo-300 transition space-y-3 shadow-2xs"
            >
              {/* Recommendation Header */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                <div className="flex flex-wrap items-center gap-2">
                  <Badge variant={getPriorityBadgeVariant(rec.priority)} size="sm" dot>
                    PRIORITY: {rec.priority}
                  </Badge>
                  <Badge variant="neutral" size="sm">
                    EFFORT: {rec.effort}
                  </Badge>
                  <Badge variant={getStatusBadgeVariant(rec.status)} size="sm">
                    {rec.status}
                  </Badge>
                  <Badge variant="neutral" size="sm">
                    {rec.category}
                  </Badge>
                  <h4 className="text-xs font-bold text-slate-900 font-sans">{rec.title}</h4>
                </div>
                <span className="font-mono text-[11px] text-indigo-600 font-semibold bg-indigo-50 px-2 py-0.5 rounded border border-indigo-100 self-start sm:self-auto">
                  {rec.ruleId}
                </span>
              </div>

              {/* Summary */}
              <p className="text-xs text-slate-600 leading-relaxed font-sans">{rec.summary}</p>

              {/* Recommended Action Container */}
              <div className="p-3 rounded-lg bg-indigo-50/40 border border-indigo-100 text-xs text-indigo-950 space-y-1">
                <div className="flex items-center gap-1.5 font-bold text-indigo-900 text-[11px]">
                  <ArrowRight className="w-3.5 h-3.5 text-indigo-600 shrink-0" />
                  <span>Recommended Remediation Action:</span>
                </div>
                <p className="text-[11px] text-slate-800 leading-relaxed font-sans">{rec.recommendedAction}</p>
              </div>

              {/* Finding Traceability Bar */}
              <div className="flex flex-wrap items-center justify-between gap-2 text-[11px] text-slate-500 font-mono pt-2 border-t border-slate-100">
                <div className="flex items-center gap-2">
                  <FileCode className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                  <span className="text-slate-800 font-semibold">
                    {rec.filePath || 'Workspace Root'}
                    {rec.lineNumber ? `:${rec.lineNumber}` : ''}
                  </span>
                </div>
                <div className="flex items-center gap-2 text-slate-400 text-[10px]">
                  <span>Finding ID: {rec.findingId ? rec.findingId.substring(0, 12) : 'N/A'}</span>
                  {rec.severity && <span>• Severity: {rec.severity}</span>}
                </div>
              </div>
            </div>
          ))
        )}
      </CardContent>
    </Card>
  );
};

export default RecommendationsSection;
