'use client';

import React, { useState } from 'react';
import Modal from '@/components/ui/Modal';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import { Finding } from '@/types/finding';
import { getSeverityBadgeVariant } from '@/lib/utils';
import { FileCode, AlertCircle, Sparkles, Check, HelpCircle, Wrench, Shield } from 'lucide-react';

export interface FindingDetailsProps {
  finding: Finding;
  onClose: () => void;
}

export const FindingDetails: React.FC<FindingDetailsProps> = ({ finding, onClose }) => {
  const [aiExpanded, setAiExpanded] = useState(true);
  const badgeVariant = getSeverityBadgeVariant(finding.severity);

  return (
    <Modal
      isOpen={true}
      onClose={onClose}
      maxWidth="4xl"
      title={finding.title}
      description={`Rule: ${finding.ruleId} • Category: ${finding.category}`}
    >
      <div className="space-y-6">
        {/* Header Badges */}
        <div className="flex flex-wrap items-center justify-between gap-3 p-3 bg-slate-50 rounded-xl border border-slate-200">
          <div className="flex items-center gap-2">
            <Badge variant={badgeVariant} dot size="md">
              {finding.severity} Severity
            </Badge>
            <span className="text-xs font-semibold text-slate-600 bg-white border border-slate-200 px-2.5 py-1 rounded-md">
              {finding.category}
            </span>
          </div>

          <div className="flex items-center gap-1.5 text-xs font-mono text-slate-700 bg-white border border-slate-200 px-3 py-1 rounded-md">
            <FileCode className="w-3.5 h-3.5 text-indigo-600" />
            <span>{finding.filePath}</span>
            <span className="text-indigo-600 font-bold">:{finding.lineNumber}</span>
          </div>
        </div>

        {/* Code Location with Highlighting */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs font-semibold text-slate-700">
            <span>Code Location</span>
            <span className="font-mono text-[11px] text-slate-400">Line {finding.lineNumber} highlighted</span>
          </div>
          <div className="bg-slate-900 text-slate-100 rounded-xl p-4 font-mono text-xs overflow-x-auto border border-slate-800 shadow-inner">
            <pre className="leading-relaxed">
              {finding.codeSnippet.split('\n').map((line, idx) => {
                const lineNum = idx + 1;
                const isOffending = lineNum === finding.highlightedLine;
                return (
                  <div
                    key={idx}
                    className={`flex items-start px-2 py-0.5 rounded ${
                      isOffending ? 'bg-rose-900/60 text-rose-200 border-l-2 border-rose-500 font-bold' : 'text-slate-300'
                    }`}
                  >
                    <span className="w-8 text-slate-600 select-none text-right pr-3">{lineNum}</span>
                    <span>{line}</span>
                  </div>
                );
              })}
            </pre>
          </div>
        </div>

        {/* Structured Technical Explanation */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-1.5">
            <div className="flex items-center gap-1.5 text-xs font-bold text-rose-900">
              <AlertCircle className="w-4 h-4 text-rose-600" />
              <span>What is wrong?</span>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">{finding.whatIsWrong}</p>
          </div>

          <div className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-1.5">
            <div className="flex items-center gap-1.5 text-xs font-bold text-amber-900">
              <Shield className="w-4 h-4 text-amber-600" />
              <span>Why does it matter?</span>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">{finding.whyItMatters}</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-1.5">
            <div className="flex items-center gap-1.5 text-xs font-bold text-slate-900">
              <HelpCircle className="w-4 h-4 text-slate-600" />
              <span>What should I review?</span>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">{finding.whatToReview}</p>
          </div>

          <div className="p-4 bg-emerald-50/60 rounded-xl border border-emerald-200 space-y-1.5">
            <div className="flex items-center gap-1.5 text-xs font-bold text-emerald-900">
              <Wrench className="w-4 h-4 text-emerald-700" />
              <span>Recommended Action</span>
            </div>
            <p className="text-xs text-emerald-950 font-medium leading-relaxed">{finding.recommendedAction}</p>
          </div>
        </div>

        {/* Dedicated AI Explanation Box */}
        <div className="rounded-xl border border-indigo-200 bg-gradient-to-br from-indigo-50/70 to-sky-50/50 p-5 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-indigo-900 font-bold text-xs">
              <Sparkles className="w-4 h-4 text-indigo-600" />
              <span>AI Technical Explanation</span>
            </div>
            <span className="text-[10px] uppercase font-bold tracking-wider text-indigo-600 bg-indigo-100/70 px-2 py-0.5 rounded">
              Generated by Analyzer RAG
            </span>
          </div>
          <p className="text-xs text-slate-700 leading-relaxed">
            {finding.aiExplanation}
          </p>
        </div>

        <div className="flex justify-end pt-2">
          <Button size="sm" onClick={onClose}>
            Close
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default FindingDetails;
