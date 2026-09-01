export const APP_NAME = 'AI Release Readiness Analyzer';
export const APP_DESCRIPTION = 'Automated AI-powered release readiness and risk scoring analyzer for modern software engineering.';

export const NAV_LINKS = [
  { href: '/dashboard', label: 'Dashboard', icon: 'LayoutDashboard' },
  { href: '/projects', label: 'Projects', icon: 'FolderGit2' },
  { href: '/releases', label: 'Releases', icon: 'GitBranch' },
  { href: '/reports', label: 'Reports', icon: 'FileText' },
  { href: '/settings', label: 'Settings', icon: 'Settings' },
];

export const PROJECT_TYPES = [
  'Web Application',
  'Mobile Application',
  'Desktop Application',
  'AI/ML Project',
  'API/Backend',
  'Other',
];

export const TECH_LANGUAGES = [
  'Java',
  'TypeScript / JavaScript',
  'Python',
  'Go',
  'C# / .NET',
  'Rust',
  'C / C++',
  'PHP',
  'Ruby',
];

export const ANALYSIS_STAGES = [
  { id: 1, name: 'Project uploaded', status: 'completed' },
  { id: 2, name: 'Detecting technology & framework', status: 'completed' },
  { id: 3, name: 'Analyzing source code & complexity', status: 'active' },
  { id: 4, name: 'Evaluating test suite & coverage', status: 'pending' },
  { id: 5, name: 'Auditing dependencies for CVEs', status: 'pending' },
  { id: 6, name: 'Running security and secret scans', status: 'pending' },
  { id: 7, name: 'Calculating weighted readiness score', status: 'pending' },
  { id: 8, name: 'Generating AI release review & insights', status: 'pending' },
];
