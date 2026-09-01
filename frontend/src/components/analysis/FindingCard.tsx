import React from 'react';
import { Card } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import { Finding } from '@/types/finding';
import { getSeverityBadgeVariant } from '@/lib/utils';
import { FileCode, ChevronRight, Sparkles } from 'lucide-react';

export interface FindingCardProps {
  finding: Finding;
  onViewDetails: (finding: Finding) => void;
}

export const FindingCard: React.FC<FindingCardProps> = ({ finding, onViewDetails }) => {
  const badgeVariant = getSeverityBadgeVariant(finding.severity);

  return (
    <Card hover className="p-4 space-y-3">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <Badge variant={badgeVariant} dot size="sm">
            {finding.severity}
          </Badge>
          <span className="text-[11px] font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded">
            {finding.category}
          </span>
          <span className="text-[10px] font-mono text-slate-400">
            {finding.ruleId}
          </span>
        </div>

        <div className="flex items-center gap-1.5 text-xs text-slate-500 font-mono">
          <FileCode className="w-3.5 h-3.5 text-slate-400" />
          <span>{finding.filePath}:{finding.lineNumber}</span>
        </div>
      </div>

      <div>
        <h4 className="text-sm font-bold text-slate-900">{finding.title}</h4>
        <p className="text-xs text-slate-600 mt-1 leading-relaxed">{finding.description}</p>
      </div>

      <div className="flex items-center justify-between pt-2 border-t border-slate-100">
        <div className="flex items-center gap-1.5 text-[11px] text-indigo-600 font-medium">
          <Sparkles className="w-3.5 h-3.5" />
          <span>AI explanation ready</span>
        </div>

        <Button
          size="sm"
          variant="outline"
          onClick={() => onViewDetails(finding)}
          rightIcon={<ChevronRight className="w-3.5 h-3.5" />}
        >
          View Details
        </Button>
      </div>
    </Card>
  );
};

export default FindingCard;
