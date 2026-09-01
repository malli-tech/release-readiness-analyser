import React from 'react';
import Link from 'next/link';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import Button from '@/components/ui/Button';
import {
  ShieldCheck,
  Sparkles,
  ArrowRight,
  CheckCircle2,
  Code2,
  TestTube2,
  Lock,
  Box,
  Cpu,
  Layers,
  UploadCloud,
  FileSearch,
  Check,
  TrendingUp,
} from 'lucide-react';

export default function LandingPage() {
  const features = [
    {
      icon: Code2,
      title: 'Project & Code Quality Analysis',
      description: 'Static analysis for cyclomatic complexity, code smells, duplicate blocks, and architectural anti-patterns.',
    },
    {
      icon: Lock,
      title: 'Security & Secret Scanning',
      description: 'Detect hardcoded production credentials, plaintext API keys, and insecure configuration endpoints.',
    },
    {
      icon: TestTube2,
      title: 'Test Coverage & Report Parsing',
      description: 'Evaluate unit, integration, and branch coverage metrics to ensure critical paths are thoroughly tested.',
    },
    {
      icon: Box,
      title: 'Dependency Vulnerability Audit',
      description: 'Cross-reference package manifests against CVE vulnerability databases to catch supply chain risks.',
    },
    {
      icon: Sparkles,
      title: 'AI-Powered Release Review',
      description: 'Contextual synthesis explaining exactly why findings matter, their exploitability, and how to resolve them.',
    },
    {
      icon: ShieldCheck,
      title: 'Weighted Release Readiness Score',
      description: 'Clear 0–100 gate scoring with configurable blockers to determine whether software is ready for deployment.',
    },
  ];

  const steps = [
    {
      num: '01',
      title: 'Upload Project',
      desc: 'Submit your repository package or source ZIP archive to our isolated local sandbox analyzer.',
    },
    {
      num: '02',
      title: 'Analyze',
      desc: 'Automated multi-layer scanner detects languages, frameworks, test suites, and security posture.',
    },
    {
      num: '03',
      title: 'Review AI Findings',
      desc: 'Inspect highlighted code snippets, risk breakdown, and actionable AI recommendations.',
    },
    {
      num: '04',
      title: 'Improve & Re-analyze',
      desc: 'Fix flagged blockers, verify progression across release versions, and confidently release.',
    },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-slate-900 text-slate-100">
      <Navbar />

      {/* Hero Section */}
      <section className="relative pt-16 pb-20 overflow-hidden border-b border-slate-800">
        <div className="absolute inset-0 bg-[radial-gradient(#38bdf8_1px,transparent_1px)] [background-size:24px_24px] opacity-15 pointer-events-none" />

        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10 text-center space-y-8">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-indigo-950/80 border border-indigo-700/50 text-indigo-300 text-xs font-semibold">
            <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
            <span>AI-Powered Automated Evaluation Platform</span>
          </div>

          <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight text-white max-w-4xl mx-auto leading-tight">
            Know if your software is <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 via-sky-300 to-emerald-400">ready to release.</span>
          </h1>

          <p className="text-base sm:text-xl text-slate-400 max-w-2xl mx-auto leading-relaxed">
            AI-powered release readiness analysis for student and developer projects. Catch critical vulnerabilities, missing test branches, and architectural blockers before deployment.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-2">
            <Link href="/dashboard">
              <Button size="lg" variant="secondary" rightIcon={<ArrowRight className="w-4 h-4" />}>
                Analyze Your Project
              </Button>
            </Link>
            <Link href="#how-it-works">
              <Button size="lg" variant="outline" className="border-slate-700 bg-slate-800/80 text-slate-200 hover:bg-slate-800">
                See How It Works
              </Button>
            </Link>
          </div>

          {/* Interactive Hero Pipeline Visual */}
          <div className="pt-10 max-w-4xl mx-auto">
            <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-6 shadow-2xl backdrop-blur">
              <div className="flex items-center justify-between border-b border-slate-800 pb-4 mb-6 text-xs text-slate-400">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-rose-500/80" />
                  <div className="w-3 h-3 rounded-full bg-amber-500/80" />
                  <div className="w-3 h-3 rounded-full bg-emerald-500/80" />
                </div>
                <span className="font-mono">analyzer-pipeline // evaluation-graph</span>
                <span className="text-emerald-400 font-mono flex items-center gap-1">
                  <CheckCircle2 className="w-3 h-3" /> Live Sandbox
                </span>
              </div>

              {/* Pipeline Flow Diagram */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4 text-left">
                <div className="p-4 rounded-xl bg-slate-900/90 border border-slate-800 space-y-2">
                  <div className="w-8 h-8 rounded-lg bg-indigo-900/50 border border-indigo-700/50 flex items-center justify-center text-indigo-400 font-bold text-xs">
                    01
                  </div>
                  <h4 className="text-sm font-bold text-white">Project Ingestion</h4>
                  <p className="text-xs text-slate-400">Multi-language ZIP archive unpack & AST parser</p>
                </div>

                <div className="p-4 rounded-xl bg-slate-900/90 border border-slate-800 space-y-2">
                  <div className="w-8 h-8 rounded-lg bg-sky-900/50 border border-sky-700/50 flex items-center justify-center text-sky-400 font-bold text-xs">
                    02
                  </div>
                  <h4 className="text-sm font-bold text-white">Static Scanners</h4>
                  <p className="text-xs text-slate-400">Security, Tests, Complexity & CVE analysis</p>
                </div>

                <div className="p-4 rounded-xl bg-slate-900/90 border border-slate-800 space-y-2">
                  <div className="w-8 h-8 rounded-lg bg-amber-900/50 border border-amber-700/50 flex items-center justify-center text-amber-400 font-bold text-xs">
                    03
                  </div>
                  <h4 className="text-sm font-bold text-white">AI Synthesis</h4>
                  <p className="text-xs text-slate-400">RAG knowledge base generates contextual review</p>
                </div>

                <div className="p-4 rounded-xl bg-emerald-950/60 border border-emerald-700/50 space-y-2">
                  <div className="w-8 h-8 rounded-lg bg-emerald-900/80 border border-emerald-600/50 flex items-center justify-center text-emerald-300 font-bold text-xs">
                    04
                  </div>
                  <h4 className="text-sm font-bold text-emerald-200">Readiness Score</h4>
                  <div className="flex items-baseline gap-2">
                    <span className="text-2xl font-black text-emerald-400">82</span>
                    <span className="text-[10px] uppercase font-bold text-emerald-300">Needs Review</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-slate-950 border-b border-slate-800">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
          <div className="text-center space-y-3">
            <h2 className="text-xs font-bold text-indigo-400 uppercase tracking-widest">
              Core Capabilities
            </h2>
            <p className="text-2xl sm:text-3xl font-extrabold text-white">
              End-to-end evaluation built for serious engineering
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {features.map((feat, idx) => {
              const Icon = feat.icon;
              return (
                <div
                  key={idx}
                  className="p-6 rounded-2xl bg-slate-900/70 border border-slate-800 hover:border-slate-700 transition space-y-3"
                >
                  <div className="w-10 h-10 rounded-xl bg-indigo-950 border border-indigo-800 flex items-center justify-center text-indigo-400">
                    <Icon className="w-5 h-5" />
                  </div>
                  <h3 className="text-base font-bold text-white">{feat.title}</h3>
                  <p className="text-xs text-slate-400 leading-relaxed">{feat.description}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section id="how-it-works" className="py-20 bg-slate-900 border-b border-slate-800">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
          <div className="text-center space-y-3">
            <h2 className="text-xs font-bold text-sky-400 uppercase tracking-widest">
              Workflow
            </h2>
            <p className="text-2xl sm:text-3xl font-extrabold text-white">
              Four steps from code to release certification
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {steps.map((s, idx) => (
              <div key={idx} className="p-6 rounded-2xl bg-slate-950 border border-slate-800 space-y-3">
                <span className="font-mono text-xl font-extrabold text-indigo-500 block">{s.num}</span>
                <h4 className="text-sm font-bold text-white">{s.title}</h4>
                <p className="text-xs text-slate-400 leading-relaxed">{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Why It Matters for Students & Developers */}
      <section className="py-20 bg-slate-950 border-b border-slate-800">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="p-8 sm:p-12 rounded-3xl bg-gradient-to-br from-indigo-950/60 to-slate-900 border border-indigo-900/50 space-y-8">
            <div className="space-y-3 text-center sm:text-left">
              <h3 className="text-xs font-bold text-indigo-400 uppercase tracking-widest">Why It Matters</h3>
              <h2 className="text-2xl sm:text-3xl font-extrabold text-white">
                Eliminate capstone & deployment anxiety
              </h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 text-xs text-slate-300">
              <div className="flex items-start gap-3 p-4 rounded-xl bg-slate-900/80 border border-slate-800">
                <Check className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
                <div>
                  <strong className="text-white block text-sm mb-1">Find problems before submission</strong>
                  Catch critical vulnerabilities, unhandled null checks, and failed assertions before evaluators or users find them.
                </div>
              </div>

              <div className="flex items-start gap-3 p-4 rounded-xl bg-slate-900/80 border border-slate-800">
                <Check className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
                <div>
                  <strong className="text-white block text-sm mb-1">Pinpoint exact offending lines</strong>
                  Get direct source file paths, line numbers, and highlighted context instead of ambiguous generic warnings.
                </div>
              </div>

              <div className="flex items-start gap-3 p-4 rounded-xl bg-slate-900/80 border border-slate-800">
                <Check className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
                <div>
                  <strong className="text-white block text-sm mb-1">AI-assisted engineering guidance</strong>
                  Receive plain-English architectural explanations on why each issue matters and how to remediate it.
                </div>
              </div>

              <div className="flex items-start gap-3 p-4 rounded-xl bg-slate-900/80 border border-slate-800">
                <Check className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
                <div>
                  <strong className="text-white block text-sm mb-1">Track version progression</strong>
                  Compare score improvements between v1.0 and v1.3 with automated regression alerts.
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Final CTA */}
      <section className="py-16 bg-slate-900 text-center">
        <div className="max-w-4xl mx-auto px-4 space-y-6">
          <h2 className="text-3xl font-extrabold text-white">
            Analyze your first release candidate today
          </h2>
          <p className="text-sm text-slate-400 max-w-lg mx-auto">
            Get instant readiness scores, security audits, and automated AI review recommendations.
          </p>
          <div className="pt-2">
            <Link href="/dashboard">
              <Button size="lg" variant="secondary" rightIcon={<ArrowRight className="w-4 h-4" />}>
                Launch Dashboard
              </Button>
            </Link>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
}
