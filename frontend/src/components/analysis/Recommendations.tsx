import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { CheckSquare, ArrowRight } from 'lucide-react';

export interface RecommendationsProps {
  recommendations: string[];
}

export const Recommendations: React.FC<RecommendationsProps> = ({ recommendations }) => {
  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-2">
          <CheckSquare className="w-4 h-4 text-indigo-600" />
          <CardTitle className="text-sm">Actionable Readiness Checklist</CardTitle>
        </div>
      </CardHeader>

      <CardContent className="space-y-2.5">
        {recommendations.map((rec, idx) => (
          <div
            key={idx}
            className="flex items-start gap-3 p-3 rounded-lg border border-slate-200 bg-slate-50/50 hover:bg-slate-50 transition text-xs"
          >
            <span className="w-5 h-5 rounded-full bg-indigo-100 text-indigo-700 font-bold text-[11px] flex items-center justify-center shrink-0 mt-0.5">
              {idx + 1}
            </span>
            <span className="text-slate-800 font-medium leading-relaxed">{rec}</span>
          </div>
        ))}
      </CardContent>
    </Card>
  );
};

export default Recommendations;
