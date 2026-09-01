import React from 'react';
import { cn } from '@/lib/utils';
import { AlertCircle, CheckCircle2, Info, AlertTriangle, X } from 'lucide-react';

export interface AlertProps {
  type?: 'info' | 'success' | 'warning' | 'error';
  title?: string;
  message: string;
  onDismiss?: () => void;
  className?: string;
}

export const Alert: React.FC<AlertProps> = ({
  type = 'info',
  title,
  message,
  onDismiss,
  className,
}) => {
  const config = {
    info: {
      bg: 'bg-sky-50 border-sky-200 text-sky-900',
      icon: <Info className="w-4 h-4 text-sky-600 mt-0.5 shrink-0" />,
    },
    success: {
      bg: 'bg-emerald-50 border-emerald-200 text-emerald-900',
      icon: <CheckCircle2 className="w-4 h-4 text-emerald-600 mt-0.5 shrink-0" />,
    },
    warning: {
      bg: 'bg-amber-50 border-amber-200 text-amber-900',
      icon: <AlertTriangle className="w-4 h-4 text-amber-600 mt-0.5 shrink-0" />,
    },
    error: {
      bg: 'bg-rose-50 border-rose-200 text-rose-900',
      icon: <AlertCircle className="w-4 h-4 text-rose-600 mt-0.5 shrink-0" />,
    },
  };

  return (
    <div
      className={cn(
        'p-3.5 rounded-lg border flex items-start justify-between gap-3 text-xs',
        config[type].bg,
        className
      )}
    >
      <div className="flex items-start gap-2.5">
        {config[type].icon}
        <div>
          {title && <p className="font-semibold mb-0.5">{title}</p>}
          <p className="opacity-90 leading-relaxed">{message}</p>
        </div>
      </div>
      {onDismiss && (
        <button onClick={onDismiss} className="p-0.5 hover:opacity-75 transition">
          <X className="w-4 h-4" />
        </button>
      )}
    </div>
  );
};

export default Alert;
