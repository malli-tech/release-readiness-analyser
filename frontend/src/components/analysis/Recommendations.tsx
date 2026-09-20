import React from 'react';
import { RecommendationsSection } from './RecommendationsSection';
import { RecommendationRecord } from '@/types/recommendation';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { CheckSquare } from 'lucide-react';

export interface RecommendationsProps {
  analysisId?: string;
  recommendations?: RecommendationRecord[] | string[];
  onRecommendationSelect?: (recommendation: RecommendationRecord) => void;
}

export const Recommendations: React.FC<RecommendationsProps> = ({
  analysisId,
  recommendations,
  onRecommendationSelect,
}) => {
  if (analysisId || (recommendations && recommendations.length > 0 && typeof recommendations[0] !== 'string')) {
    return (
      <RecommendationsSection
        analysisId={analysisId}
        recommendations={recommendations as RecommendationRecord[]}
        onRecommendationSelect={onRecommendationSelect}
      />
    );
  }

  const legacyRecs = (recommendations as string[]) || [];

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center gap-2">
          <CheckSquare className="w-4 h-4 text-indigo-600" />
          <CardTitle className="text-sm">Actionable Readiness Checklist</CardTitle>
        </div>
      </CardHeader>

      <CardContent className="space-y-2.5">
        {legacyRecs.map((rec, idx) => (
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

export { RecommendationsSection };
export default Recommendations;
