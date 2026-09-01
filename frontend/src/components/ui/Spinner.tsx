import React from 'react';
import { cn } from '@/lib/utils';
import { Loader2 } from 'lucide-react';

export interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
  label?: string;
}

export const Spinner: React.FC<SpinnerProps> = ({ size = 'md', className, label }) => {
  const sizes = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8',
  };

  return (
    <div className="inline-flex items-center justify-center gap-2">
      <Loader2 className={cn('animate-spin text-indigo-600', sizes[size], className)} />
      {label && <span className="text-xs text-slate-600 font-medium">{label}</span>}
    </div>
  );
};

export default Spinner;
