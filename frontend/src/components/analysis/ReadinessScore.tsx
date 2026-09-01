import React from 'react';
import Badge from '@/components/ui/Badge';
import { getScoreColor } from '@/lib/utils';

export interface ReadinessScoreProps {
  score: number;
  status: string;
  size?: 'sm' | 'md' | 'lg';
  showLabel?: boolean;
}

export const ReadinessScore: React.FC<ReadinessScoreProps> = ({
  score,
  status,
  size = 'lg',
  showLabel = true,
}) => {
  const scoreConfig = getScoreColor(score);

  const dimensions = {
    sm: { radius: 36, stroke: 6, sizeClass: 'w-24 h-24', textClass: 'text-xl', subClass: 'text-[9px]' },
    md: { radius: 52, stroke: 8, sizeClass: 'w-32 h-32', textClass: 'text-2xl', subClass: 'text-[10px]' },
    lg: { radius: 68, stroke: 10, sizeClass: 'w-44 h-44', textClass: 'text-4xl', subClass: 'text-xs' },
  };

  const { radius, stroke, sizeClass, textClass, subClass } = dimensions[size];
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (score / 100) * circumference;

  return (
    <div className="flex flex-col items-center justify-center text-center space-y-3">
      <div className={`relative flex items-center justify-center ${sizeClass}`}>
        <svg className="w-full h-full transform -rotate-90">
          <circle
            cx="50%"
            cy="50%"
            r={radius}
            className="stroke-slate-100"
            strokeWidth={stroke}
            fill="transparent"
          />
          <circle
            cx="50%"
            cy="50%"
            r={radius}
            className={score >= 90 ? 'stroke-emerald-500' : score >= 70 ? 'stroke-amber-500' : 'stroke-rose-500'}
            strokeWidth={stroke}
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            fill="transparent"
            style={{ transition: 'stroke-dashoffset 1s cubic-bezier(0.4, 0, 0.2, 1)' }}
          />
        </svg>

        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className={`font-extrabold text-slate-900 tracking-tight ${textClass}`}>
            {score}
          </span>
          <span className={`font-semibold text-slate-400 uppercase tracking-wider ${subClass}`}>
            out of 100
          </span>
        </div>
      </div>

      {showLabel && (
        <div className="space-y-1">
          <Badge variant={scoreConfig.badge} dot size="md">
            {status}
          </Badge>
        </div>
      )}
    </div>
  );
};

export default ReadinessScore;
