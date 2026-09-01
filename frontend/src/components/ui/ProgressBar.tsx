import React from 'react';
import { cn } from '@/lib/utils';

export interface ProgressBarProps {
  value: number;
  max?: number;
  label?: string;
  showValue?: boolean;
  size?: 'sm' | 'md' | 'lg';
  variant?: 'emerald' | 'amber' | 'rose' | 'indigo' | 'auto';
  className?: string;
}

export const ProgressBar: React.FC<ProgressBarProps> = ({
  value,
  max = 100,
  label,
  showValue = false,
  size = 'md',
  variant = 'auto',
  className,
}) => {
  const percentage = Math.min(Math.max(Math.round((value / max) * 100), 0), 100);

  let barColor = 'bg-indigo-600';
  if (variant === 'auto') {
    if (percentage >= 85) barColor = 'bg-emerald-500';
    else if (percentage >= 70) barColor = 'bg-amber-500';
    else barColor = 'bg-rose-500';
  } else if (variant === 'emerald') barColor = 'bg-emerald-500';
  else if (variant === 'amber') barColor = 'bg-amber-500';
  else if (variant === 'rose') barColor = 'bg-rose-500';
  else if (variant === 'indigo') barColor = 'bg-indigo-600';

  const heights = {
    sm: 'h-1.5',
    md: 'h-2.5',
    lg: 'h-4',
  };

  return (
    <div className={cn('w-full space-y-1.5', className)}>
      {(label || showValue) && (
        <div className="flex justify-between items-center text-xs text-slate-600 font-medium">
          {label && <span>{label}</span>}
          {showValue && <span>{percentage}%</span>}
        </div>
      )}
      <div className={cn('w-full bg-slate-100 rounded-full overflow-hidden', heights[size])}>
        <div
          className={cn('h-full transition-all duration-500 rounded-full', barColor)}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
};

export default ProgressBar;
