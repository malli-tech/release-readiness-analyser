'use client';

import { useState } from 'react';
import { mockAnalysisResult, mockFindings } from '@/lib/mock-data';
import { AnalysisResult } from '@/types/analysis';
import { Finding, FindingSeverity, FindingCategory } from '@/types/finding';

export const useAnalysis = (_releaseId?: string) => {
  const [result, setResult] = useState<AnalysisResult>(mockAnalysisResult);
  const [selectedSeverity, setSelectedSeverity] = useState<string>('ALL');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [selectedFinding, setSelectedFinding] = useState<Finding | null>(null);

  const filteredFindings = mockFindings.filter((f) => {
    const matchSev = selectedSeverity === 'ALL' || f.severity === selectedSeverity;
    const matchCat = selectedCategory === 'ALL' || f.category === selectedCategory;
    return matchSev && matchCat;
  });

  return {
    analysis: result,
    findings: filteredFindings,
    allFindings: mockFindings,
    selectedSeverity,
    setSelectedSeverity,
    selectedCategory,
    setSelectedCategory,
    selectedFinding,
    setSelectedFinding,
  };
};
