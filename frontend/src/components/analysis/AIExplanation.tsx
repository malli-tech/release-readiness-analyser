import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { AIReview } from '@/types/analysis';
import { Sparkles, AlertOctagon, CheckCircle2, ShieldAlert } from 'lucide-react';

export interface AIExplanationProps {
  aiReview: AIReview;
}

export const AIExplanation: React.FC<AIExplanationProps> = ({ aiReview }) => {
  return (
    <Card className="border-indigo-200 bg-gradient-to-br from-white via-indigo-50/20 to-sky-50/30 overflow-hidden shadow-sm">
      <CardHeader className="bg-indigo-900 text-white flex flex-row items-center justify-between py-4">
        <div className="flex items-center gap-2.5">
          <div className="p-1.5 rounded-lg bg-indigo-800 text-indigo-200">
            <Sparkles className="w-5 h-5 text-amber-300" />
          </div>
          <div>
            <CardTitle className="text-white text-base">AI Release Review</CardTitle>
            <p className="text-[11px] text-indigo-200">
              Automated synthesis based on static findings, dependency graph, and security posture
            </p>
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-6 space-y-6">
        {/* Executive Summary */}
        <div className="space-y-2">
          <span className="text-xs font-bold text-slate-900 uppercase tracking-wider">
            Executive Summary
          </span>
          <p className="text-xs text-slate-700 leading-relaxed bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
            {aiReview.summary}
          </p>
        </div>

        {/* Major Concerns & Blockers */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 space-y-2">
            <div className="flex items-center gap-2 text-xs font-bold text-rose-900">
              <ShieldAlert className="w-4 h-4 text-rose-600" />
              <span>Major Release Concerns</span>
            </div>
            <ul className="space-y-1.5 text-xs text-rose-950">
              {aiReview.majorConcerns.map((concern, idx) => (
                <li key={idx} className="flex items-start gap-1.5">
                  <span className="text-rose-500 font-bold">•</span>
                  <span>{concern}</span>
                </li>
              ))}
            </ul>
          </div>

          <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 space-y-2">
            <div className="flex items-center gap-2 text-xs font-bold text-emerald-900">
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
              <span>Recommended Remediation Path</span>
            </div>
            <ul className="space-y-1.5 text-xs text-emerald-950">
              {aiReview.recommendedActions.map((action, idx) => (
                <li key={idx} className="flex items-start gap-1.5">
                  <span className="text-emerald-500 font-bold">•</span>
                  <span>{action}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default AIExplanation;
