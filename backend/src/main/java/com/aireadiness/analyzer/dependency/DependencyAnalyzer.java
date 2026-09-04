package com.aireadiness.analyzer.dependency;

import com.aireadiness.analyzer.Analyzer;
import com.aireadiness.analyzer.dependency.model.DependencyInfo;
import com.aireadiness.analyzer.dependency.model.DependencyManifestInfo;
import com.aireadiness.analyzer.dependency.rules.*;
import com.aireadiness.model.DependencySummary;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class DependencyAnalyzer implements Analyzer {

    private final long maxFileSizeBytes;
    private final int maxManifestFiles;
    private final int maxTotalFindings;

    private final List<DependencyRule> rules;
    private DependencySummary lastSummary;

    private static final Set<String> MANIFEST_FILENAMES = Set.of(
            "pom.xml", "build.gradle", "build.gradle.kts", "settings.gradle", "settings.gradle.kts",
            "package.json", "package-lock.json", "npm-shrinkwrap.json", "yarn.lock", "pnpm-lock.yaml",
            "requirements.txt", "requirements-dev.txt", "pyproject.toml", "pipfile", "pipfile.lock", "poetry.lock",
            "go.mod", "go.sum", "packages.config", "directory.packages.props", "composer.json", "composer.lock"
    );

    private static final Pattern SECRET_PATTERN = Pattern.compile(
            "(?i)(password|secret|apikey|api_key|token|auth_token|private_key|aws_key)\\s*[:=]\\s*[\"']([^\"']+)[\"']"
    );

    public DependencyAnalyzer(
            @Value("${app.analyzer.max-file-size-mb:2}") int maxFileSizeMb,
            @Value("${app.analyzer.max-manifest-files:100}") int maxManifestFiles,
            @Value("${app.analyzer.max-total-findings:2000}") int maxTotalFindings
    ) {
        this.maxFileSizeBytes = (long) maxFileSizeMb * 1024 * 1024;
        this.maxManifestFiles = maxManifestFiles;
        this.maxTotalFindings = maxTotalFindings;

        this.rules = List.of(
                new MissingDependencyManifestRule(),
                new UnpinnedDependencyRule(),
                new BroadVersionRule(),
                new DuplicateDependencyRule(),
                new DependencyVersionInconsistencyRule(),
                new DependencyManifestWarningRule()
        );
    }

    @Override
    public String getType() {
        return "DEPENDENCIES";
    }

    public DependencySummary getLastSummary() {
        return lastSummary;
    }

    @Override
    public List<Finding> analyze(Path workspaceDir, ProjectProfile profile, String analysisId, String uploadMode, List<String> warnings) {
        List<Finding> findings = new ArrayList<>();
        Set<String> findingDeduplicationKeys = new HashSet<>();

        DependencySummary summary = new DependencySummary();
        this.lastSummary = summary;

        if (workspaceDir == null || !Files.exists(workspaceDir) || !Files.isDirectory(workspaceDir)) {
            warnings.add("Workspace directory is unavailable for static dependency analysis.");
            summary.setDependencyCompleteness("UNKNOWN");
            summary.getDependencyWarnings().add("Workspace directory is unavailable.");
            return findings;
        }

        if ("SELECTED_CONTENT".equalsIgnoreCase(uploadMode)) {
            summary.getDependencyWarnings().add("Analysis covers only uploaded content. Dependency files may exist outside the uploaded files.");
        }

        if (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType())) {
            warnings.add("Dependency analysis could not identify a supported project ecosystem.");
            summary.getDependencyWarnings().add("Dependency analysis is not currently supported or project ecosystem is unknown.");
            summary.setDependencyCompleteness("UNKNOWN");
            return findings;
        }

        List<DependencyManifestInfo> manifests = new ArrayList<>();
        List<DependencyInfo> allDependencies = new ArrayList<>();
        Set<String> manifestPaths = new LinkedHashSet<>();
        Set<String> packageManagers = new LinkedHashSet<>();

        int manifestCount = 0;

        try {
            List<Path> allPaths = new ArrayList<>();
            try (var stream = Files.walk(workspaceDir)) {
                stream.forEach(allPaths::add);
            }

            for (Path path : allPaths) {
                if (Files.isDirectory(path)) continue;

                Path relativePath = workspaceDir.relativize(path).normalize();
                if (relativePath.toString().startsWith("..")) continue;

                String relPathStr = relativePath.toString().replace('\\', '/');
                String fnLower = path.getFileName().toString().toLowerCase();

                boolean isManifest = MANIFEST_FILENAMES.contains(fnLower) || fnLower.endsWith(".csproj");
                if (!isManifest) continue;

                manifestCount++;
                if (manifestCount > maxManifestFiles) {
                    warnings.add("Exceeded maximum manifest file count limit (" + maxManifestFiles + "). Remaining manifests skipped.");
                    break;
                }

                long fileSize = Files.size(path);
                if (fileSize > maxFileSizeBytes) {
                    warnings.add("Manifest skipped because it exceeds static analysis size limit: " + relPathStr);
                    continue;
                }

                DependencyManifestInfo manifestInfo = DependencyParser.parseManifest(path, workspaceDir);
                manifests.add(manifestInfo);
                manifestPaths.add(relPathStr);

                if (manifestInfo.getPackageManager() != null) {
                    packageManagers.add(manifestInfo.getPackageManager());
                }

                allDependencies.addAll(manifestInfo.getDependencies());
            }
        } catch (Exception e) {
            warnings.add("Static dependency analysis encountered an error while discovering manifests: " + e.getMessage());
        }

        // Aggregate statistics for DependencySummary
        summary.setManifestFiles(new ArrayList<>(manifestPaths));
        summary.setDetectedPackageManagers(new ArrayList<>(packageManagers));
        summary.setDependencyCount(allDependencies.size());

        int directCount = 0;
        int devCount = 0;
        int unpinnedCount = 0;
        int broadVersionCount = 0;

        for (DependencyInfo dep : allDependencies) {
            if ("DIRECT".equalsIgnoreCase(dep.getScope())) directCount++;
            if ("DEV".equalsIgnoreCase(dep.getScope())) devCount++;
            if ("UNPINNED".equalsIgnoreCase(dep.getVersionType())) unpinnedCount++;
            if ("BROAD_RANGE".equalsIgnoreCase(dep.getVersionType())) broadVersionCount++;
        }

        summary.setDirectDependencyCount(directCount);
        summary.setDevDependencyCount(devCount);
        summary.setUnpinnedDependencyCount(unpinnedCount);
        summary.setBroadVersionDependencyCount(broadVersionCount);

        // Determine Dependency Completeness
        String completeness = calculateCompleteness(manifests, allDependencies, uploadMode, profile);
        summary.setDependencyCompleteness(completeness);

        DependencyContext context = new DependencyContext(
                workspaceDir,
                profile,
                analysisId,
                uploadMode,
                manifests,
                allDependencies,
                summary,
                warnings
        );

        // Evaluate dependency rules
        for (DependencyRule rule : rules) {
            if (findings.size() >= maxTotalFindings) {
                warnings.add("Maximum dependency analysis findings count reached (" + maxTotalFindings + "). Further findings truncated.");
                break;
            }

            try {
                List<Finding> ruleFindings = rule.evaluate(context);
                for (Finding f : ruleFindings) {
                    if (f.getRuleId().equals("DEPENDENCY_DUPLICATE")) {
                        summary.setDuplicateDependencyCount(summary.getDuplicateDependencyCount() + 1);
                    }

                    String dedupKey = analysisId + ":" + f.getRuleId() + ":" + (f.getFilePath() != null ? f.getFilePath() : "") + ":" + (f.getLineNumber() != null ? f.getLineNumber() : 0) + ":" + (f.getEvidence() != null ? f.getEvidence().hashCode() : 0);
                    if (findingDeduplicationKeys.add(dedupKey)) {
                        redactSecretsInFinding(f);
                        findings.add(f);
                    }
                }
            } catch (Exception e) {
                warnings.add("Dependency rule " + rule.getRuleId() + " failed: " + e.getMessage());
            }
        }

        return findings;
    }

    private String calculateCompleteness(List<DependencyManifestInfo> manifests, List<DependencyInfo> dependencies, String uploadMode, ProjectProfile profile) {
        if (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType())) {
            return "UNKNOWN";
        }

        if ("SELECTED_CONTENT".equalsIgnoreCase(uploadMode)) {
            if (!manifests.isEmpty()) {
                return "PARTIAL";
            }
            return "UNKNOWN";
        }

        // COMPLETE_PROJECT
        if (manifests.isEmpty()) {
            return "UNKNOWN";
        }

        long unpinned = dependencies.stream().filter(d -> "UNPINNED".equalsIgnoreCase(d.getVersionType())).count();
        if (unpinned > 0) {
            return "PARTIAL";
        }

        return "COMPLETE";
    }

    private void redactSecretsInFinding(Finding finding) {
        if (finding.getEvidence() != null) {
            finding.setEvidence(SECRET_PATTERN.matcher(finding.getEvidence()).replaceAll("$1=***REDACTED***"));
        }
        if (finding.getDescription() != null) {
            finding.setDescription(SECRET_PATTERN.matcher(finding.getDescription()).replaceAll("$1=***REDACTED***"));
        }
    }
}
