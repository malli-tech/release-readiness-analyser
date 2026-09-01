import { User } from '@/types/user';
import { Project } from '@/types/project';
import { Release } from '@/types/release';
import { Finding } from '@/types/finding';
import { AnalysisResult } from '@/types/analysis';

export const mockCurrentUser: User = {
  id: 'usr-001',
  name: 'Alex Rivera',
  email: 'alex.rivera@university.edu',
  role: 'Student Developer',
  avatarUrl: '',
  createdAt: '2026-01-15',
  projectsCount: 4,
  institution: 'Department of Computer Science',
};

export const mockProjects: Project[] = [
  {
    id: 'proj-001',
    name: 'Student Management System',
    description: 'Full-stack academic record and enrollment management platform with real-time audit logging.',
    language: 'Java',
    framework: 'Spring Boot 3.3',
    type: 'Web Application',
    latestVersion: 'v1.3',
    latestScore: 87,
    status: 'NEEDS_REVIEW',
    lastAnalyzed: '2026-08-29 14:32',
    releasesCount: 4,
    issuesCount: 6,
    tags: ['Java', 'Spring Boot', 'MongoDB', 'React'],
  },
  {
    id: 'proj-002',
    name: 'Cloud E-Commerce Platform',
    description: 'High-throughput online store with distributed order processing, catalog search, and cart synchronization.',
    language: 'TypeScript',
    framework: 'Next.js & Node.js',
    type: 'Web Application',
    latestVersion: 'v2.1',
    latestScore: 92,
    status: 'READY',
    lastAnalyzed: '2026-08-28 18:15',
    releasesCount: 6,
    issuesCount: 3,
    tags: ['TypeScript', 'Next.js', 'PostgreSQL', 'Tailwind'],
  },
  {
    id: 'proj-003',
    name: 'Healthcare IoT Gateway',
    description: 'Telemetry ingestion and vital sign alert routing bridge for remote patient monitoring devices.',
    language: 'Python',
    framework: 'FastAPI',
    type: 'API/Backend',
    latestVersion: 'v1.0',
    latestScore: 54,
    status: 'NOT_READY',
    lastAnalyzed: '2026-08-27 11:20',
    releasesCount: 2,
    issuesCount: 14,
    tags: ['Python', 'FastAPI', 'Redis', 'MQTT'],
  },
  {
    id: 'proj-004',
    name: 'AI Code Review Assistant',
    description: 'Semantic diff analyzer using vector embeddings to detect architectural anti-patterns.',
    language: 'Python',
    framework: 'PyTorch & FastAPI',
    type: 'AI/ML Project',
    latestVersion: 'v0.9',
    latestScore: 78,
    status: 'NEEDS_REVIEW',
    lastAnalyzed: '2026-08-26 09:45',
    releasesCount: 3,
    issuesCount: 8,
    tags: ['Python', 'RAG', 'VectorDB', 'LangChain'],
  },
];

export const mockReleases: Release[] = [
  {
    id: 'rel-101',
    projectId: 'proj-001',
    projectName: 'Student Management System',
    version: 'v1.3',
    name: 'Audit Logging & Final Submission Candidate',
    description: 'Added immutable audit trail and security hardening before capstone evaluation.',
    createdAt: '2026-08-29',
    readinessScore: 87,
    status: 'NEEDS_REVIEW',
    riskLevel: 'MEDIUM',
    issuesCount: { critical: 0, high: 2, medium: 4, low: 5, total: 11 },
    fileSize: '14.2 MB',
    commitHash: '7f9a2bc',
  },
  {
    id: 'rel-102',
    projectId: 'proj-001',
    projectName: 'Student Management System',
    version: 'v1.2',
    name: 'Course Registration Module',
    description: 'Implemented multi-semester prerequisite validation algorithm.',
    createdAt: '2026-08-20',
    readinessScore: 82,
    status: 'NEEDS_REVIEW',
    riskLevel: 'MEDIUM',
    issuesCount: { critical: 1, high: 3, medium: 7, low: 12, total: 23 },
    fileSize: '13.8 MB',
    commitHash: '3e41b99',
  },
  {
    id: 'rel-103',
    projectId: 'proj-001',
    projectName: 'Student Management System',
    version: 'v1.1',
    name: 'Authentication & RBAC Overhaul',
    description: 'Migrated session tokens to stateless JWT authentication filter.',
    createdAt: '2026-08-10',
    readinessScore: 72,
    status: 'NOT_READY',
    riskLevel: 'HIGH',
    issuesCount: { critical: 3, high: 8, medium: 9, low: 14, total: 34 },
    fileSize: '12.4 MB',
    commitHash: '1a2b3c4',
  },
  {
    id: 'rel-104',
    projectId: 'proj-001',
    projectName: 'Student Management System',
    version: 'v1.0',
    name: 'Initial Prototype Baseline',
    description: 'Initial project scaffolding and fundamental entity CRUD models.',
    createdAt: '2026-07-25',
    readinessScore: 62,
    status: 'NOT_READY',
    riskLevel: 'CRITICAL',
    issuesCount: { critical: 5, high: 11, medium: 15, low: 18, total: 49 },
    fileSize: '10.1 MB',
    commitHash: '0f8c7d1',
  },
  {
    id: 'rel-201',
    projectId: 'proj-002',
    projectName: 'Cloud E-Commerce Platform',
    version: 'v2.1',
    name: 'Stripe Webhook & Payment Resiliency',
    description: 'Idempotent payment webhook consumers and distributed transaction rollback.',
    createdAt: '2026-08-28',
    readinessScore: 92,
    status: 'READY',
    riskLevel: 'LOW',
    issuesCount: { critical: 0, high: 0, medium: 3, low: 4, total: 7 },
    fileSize: '28.6 MB',
    commitHash: '98d7f6a',
  },
  {
    id: 'rel-301',
    projectId: 'proj-003',
    projectName: 'Healthcare IoT Gateway',
    version: 'v1.0',
    name: 'Initial Device Ingestion Release',
    description: 'High frequency telemetry websocket listeners.',
    createdAt: '2026-08-27',
    readinessScore: 54,
    status: 'NOT_READY',
    riskLevel: 'CRITICAL',
    issuesCount: { critical: 4, high: 6, medium: 4, low: 8, total: 22 },
    fileSize: '8.4 MB',
    commitHash: '5e4d3c2',
  },
];

export const mockFindings: Finding[] = [
  {
    id: 'find-001',
    releaseId: 'rel-102',
    title: 'Hardcoded Database Credential in Configuration',
    category: 'Security',
    severity: 'CRITICAL',
    filePath: 'src/config/database.js',
    lineNumber: 14,
    description: 'Plaintext administrative database password detected directly inside configuration source file.',
    codeSnippet: `const mongoose = require('mongoose');

// Database Connection Configuration
const dbConfig = {
  host: process.env.DB_HOST || 'localhost',
  port: process.env.DB_PORT || 27017,
  database: 'production_aireadiness',
  username: 'admin',
  password: 'SuperSecretProductionPassword123!', // <-- VIOLATION
  ssl: true
};

module.exports = dbConfig;`,
    highlightedLine: 9,
    whatIsWrong: 'A sensitive credential is committed directly into the source control repository rather than supplied via environment variables.',
    whyItMatters: 'Any developer, collaborator, or leaked repository clone exposes unrestricted access to your production database, leading to data exfiltration and credential stuffing attacks.',
    whatToReview: 'Inspect all configuration files and repository git history for committed secrets.',
    recommendedAction: 'Extract the credential to an environment variable (`DB_PASSWORD`) and immediately rotate the exposed database password in MongoDB Atlas.',
    aiExplanation: 'The static scanner identified a high-entropy string assigned to `password` in `src/config/database.js:14`. Best practice dictates all secrets must be loaded at runtime via environment variables or a secrets manager.',
    ruleId: 'SEC-SECRETS-001',
  },
  {
    id: 'find-002',
    releaseId: 'rel-102',
    title: 'Known Remote Code Execution in Dependency jsonwebtoken',
    category: 'Dependencies',
    severity: 'HIGH',
    filePath: 'package.json',
    lineNumber: 22,
    description: 'Dependency jsonwebtoken@8.5.1 contains CVE-2022-23529 permitting arbitrary code execution during secret verification.',
    codeSnippet: `{
  "name": "student-management-api",
  "version": "1.2.0",
  "dependencies": {
    "express": "^4.18.2",
    "jsonwebtoken": "8.5.1",
    "mongoose": "^7.5.0"
  }
}`,
    highlightedLine: 6,
    whatIsWrong: 'The project is using a vulnerable version of `jsonwebtoken` vulnerable to malicious key object exploits.',
    whyItMatters: 'Attackers can forge crafted JWT signatures or exploit insecure key parsing to trigger arbitrary code execution on your Node backend.',
    whatToReview: 'Check all token signing and verification middleware in `src/middleware/auth.ts`.',
    recommendedAction: 'Upgrade `jsonwebtoken` to version `>=9.0.0` and execute `npm audit fix`.',
    aiExplanation: 'The dependency vulnerability analyzer cross-referenced `jsonwebtoken@8.5.1` with the National Vulnerability Database (NVD). Upgrade to `^9.0.2` to neutralize this vector.',
    ruleId: 'DEP-CVE-2022-23529',
  },
  {
    id: 'find-003',
    releaseId: 'rel-102',
    title: 'Uncovered Critical Edge Case in Course Registration Service',
    category: 'Testing',
    severity: 'HIGH',
    filePath: 'src/services/RegistrationService.java',
    lineNumber: 88,
    description: 'Branch coverage for concurrent enrollment limit checks is 0%. No unit tests cover race condition seat allocation.',
    codeSnippet: `public synchronized EnrollmentResult enrollStudent(String studentId, String courseId) {
    Course course = courseRepository.findById(courseId).orElseThrow();
    
    // Concurrency edge case not covered by unit tests
    if (course.getCurrentEnrolled() >= course.getMaxCapacity()) {
        return EnrollmentResult.failed("Course is full");
    }
    
    course.incrementEnrolled();
    return courseRepository.save(course);
}`,
    highlightedLine: 5,
    whatIsWrong: 'Concurrent over-capacity boundary condition is completely untested.',
    whyItMatters: 'Under simultaneous student registration submissions, race conditions can cause courses to exceed physical classroom seating capacity.',
    whatToReview: '`RegistrationServiceTest.java` suite and add multi-threaded concurrent test cases.',
    recommendedAction: 'Add a parameter-driven unit test verifying `EnrollmentResult.failed` when `currentEnrolled == maxCapacity`.',
    aiExplanation: 'Static branch analysis determined that the `currentEnrolled >= maxCapacity` decision branch has zero executing assertions in the current test reports.',
    ruleId: 'TEST-BRANCH-COV-04',
  },
  {
    id: 'find-004',
    releaseId: 'rel-102',
    title: 'Cyclomatic Complexity Exceeds Threshold in GradeCalculator',
    category: 'Code Quality',
    severity: 'MEDIUM',
    filePath: 'src/utils/GradeCalculator.java',
    lineNumber: 34,
    description: 'Method `calculateFinalGPA` has cyclomatic complexity of 18 (recommended maximum: 10).',
    codeSnippet: `public double calculateFinalGPA(List<GradeRecord> records, AcademicTerm term) {
    double totalPoints = 0.0;
    int totalCredits = 0;
    
    for (GradeRecord r : records) {
        if (r.isAudited()) continue;
        if (r.isWithdrawn()) {
            if (term.isStrict()) totalPoints -= 0.5;
            continue;
        }
        if (r.getGrade().equals("A+")) totalPoints += 4.0 * r.getCredits();
        else if (r.getGrade().equals("A")) totalPoints += 4.0 * r.getCredits();
        else if (r.getGrade().equals("A-")) totalPoints += 3.7 * r.getCredits();
        // 12 more nested if/else statements...
    }
    return totalPoints / totalCredits;
}`,
    highlightedLine: 10,
    whatIsWrong: 'Deep nested conditional ladder violates single responsibility and maintainability thresholds.',
    whyItMatters: 'High complexity increases regression risk during academic policy updates and makes debugging edge cases difficult.',
    whatToReview: 'Refactor using a Lookup Table (Enum Map) for grade point conversions.',
    recommendedAction: 'Replace if-else ladder with a `GradeScale.fromGrade(grade).getGpaValue()` map lookup.',
    aiExplanation: 'Refactoring this method into small modular lookup strategies will reduce cyclomatic complexity from 18 to 3.',
    ruleId: 'CODE-COMPLEXITY-018',
  },
  {
    id: 'find-005',
    releaseId: 'rel-102',
    title: 'Unbounded Query Execution Missing Pagination',
    category: 'Performance',
    severity: 'MEDIUM',
    filePath: 'src/controllers/StudentController.java',
    lineNumber: 52,
    description: 'Endpoint `/api/students` executes `findAll()` without limit or offset pagination.',
    codeSnippet: `@GetMapping("/students")
public ResponseEntity<List<StudentDTO>> getAllStudents() {
    // Unbounded query without Pageable parameter
    List<Student> students = studentRepository.findAll();
    return ResponseEntity.ok(studentMapper.toDTOList(students));
}`,
    highlightedLine: 4,
    whatIsWrong: 'Retrieving thousands of documents in memory will cause high JVM heap pressure.',
    whyItMatters: 'As student records grow, this endpoint will cause High Garbage Collection latency and eventual `OutOfMemoryError`.',
    whatToReview: 'Add Spring Data `Pageable pageable` parameter to repository and controller.',
    recommendedAction: 'Modify signature to `Page<StudentDTO> getAllStudents(Pageable pageable)`.',
    aiExplanation: 'Unbounded `findAll()` queries degrade database response times linearly ($O(N)$). Pagination guarantees consistent sub-100ms response time.',
    ruleId: 'PERF-PAGINATION-002',
  },
];

export const mockAnalysisResult: AnalysisResult = {
  id: 'anl-882',
  releaseId: 'rel-102',
  projectId: 'proj-001',
  readinessScore: 82,
  status: 'NEEDS_REVIEW',
  createdAt: '2026-08-29 14:32:10 UTC',
  categoryScores: [
    {
      category: 'Testing',
      score: 92,
      weight: 25,
      status: 'Good',
      issuesCount: 1,
      description: '88% line coverage across 142 automated tests. 1 edge-case branch missing.',
    },
    {
      category: 'Security',
      score: 74,
      weight: 30,
      status: 'Needs Attention',
      issuesCount: 1,
      description: '1 critical plaintext credential detected. No SQL injection vulnerabilities found.',
    },
    {
      category: 'Code Quality',
      score: 86,
      weight: 20,
      status: 'Good',
      issuesCount: 1,
      description: 'Low duplication rate (1.8%). 1 complex method flagged for refactoring.',
    },
    {
      category: 'Dependencies',
      score: 70,
      weight: 15,
      status: 'Needs Attention',
      issuesCount: 1,
      description: '1 high-severity CVE found in jsonwebtoken. 3 outdated minor packages.',
    },
    {
      category: 'Performance',
      score: 88,
      weight: 10,
      status: 'Good',
      issuesCount: 1,
      description: 'Zero N+1 database queries detected. 1 unbounded listing endpoint flagged.',
    },
  ],
  findingSummary: {
    critical: 1,
    high: 3,
    medium: 7,
    low: 12,
    total: 23,
  },
  topFindings: mockFindings,
  aiReview: {
    summary: 'Your project is close to release readiness (Score: 82/100), but 1 critical security finding (hardcoded credential) and 1 vulnerable dependency (jsonwebtoken) block immediate production deployment.',
    majorConcerns: [
      'Plaintext secret located in src/config/database.js must be rotated and sanitized immediately.',
      'Known CVE in jsonwebtoken allows potential remote token manipulation.',
      'Course enrollment limit logic lacks concurrent stress tests.',
    ],
    recommendedActions: [
      'Extract database credentials to environment variables and rotate Atlas admin user.',
      'Upgrade jsonwebtoken dependency to ^9.0.2 to patch CVE-2022-23529.',
      'Add parameter-driven unit tests for boundary capacity in RegistrationService.',
      'Apply Spring Data Pageable pagination on /api/students endpoint.',
    ],
    blockers: [
      'CRITICAL: Hardcoded Credential in src/config/database.js',
      'HIGH: CVE-2022-23529 in jsonwebtoken@8.5.1',
    ],
  },
  recommendations: [
    'Remove hardcoded credentials and configure secure secret injection.',
    'Resolve vulnerable dependency jsonwebtoken via npm update.',
    'Increase branch test coverage for concurrency boundary conditions.',
    'Refactor GradeCalculator.calculateFinalGPA to reduce cyclomatic complexity.',
    'Enforce pagination on all database retrieval endpoints.',
  ],
  comparison: {
    previousVersion: 'v1.1',
    currentVersion: 'v1.2',
    metrics: [
      { name: 'Readiness Score', previousValue: 72, currentValue: 82, change: 10, isPositiveChange: true },
      { name: 'Critical Issues', previousValue: 3, currentValue: 1, change: -2, isPositiveChange: true },
      { name: 'High Issues', previousValue: 8, currentValue: 3, change: -5, isPositiveChange: true },
      { name: 'Test Coverage', previousValue: 74, currentValue: 88, change: 14, unit: '%', isPositiveChange: true },
      { name: 'Security Score', previousValue: 68, currentValue: 74, change: 6, isPositiveChange: true },
      { name: 'Code Duplication', previousValue: 4.2, currentValue: 1.8, change: -2.4, unit: '%', isPositiveChange: true },
    ],
  },
};

export const mockReports = [
  {
    id: 'rep-001',
    projectName: 'Student Management System',
    releaseVersion: 'v1.3',
    score: 87,
    status: 'NEEDS_REVIEW',
    generatedDate: '2026-08-29 14:32',
    size: '2.4 MB',
    format: 'PDF',
  },
  {
    id: 'rep-002',
    projectName: 'Student Management System',
    releaseVersion: 'v1.2',
    score: 82,
    status: 'NEEDS_REVIEW',
    generatedDate: '2026-08-20 16:10',
    size: '2.1 MB',
    format: 'PDF',
  },
  {
    id: 'rep-003',
    projectName: 'Cloud E-Commerce Platform',
    releaseVersion: 'v2.1',
    score: 92,
    status: 'READY',
    generatedDate: '2026-08-28 18:15',
    size: '3.6 MB',
    format: 'PDF',
  },
  {
    id: 'rep-004',
    projectName: 'Healthcare IoT Gateway',
    releaseVersion: 'v1.0',
    score: 54,
    status: 'NOT_READY',
    generatedDate: '2026-08-27 11:20',
    size: '1.9 MB',
    format: 'PDF',
  },
];
