package com.aireadiness.analyzer.quality.rules;

import com.aireadiness.analyzer.quality.QualityRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;

public class EmptyCatchRule implements QualityRule {

    @Override
    public String getRuleId() {
        return "CODE_QUALITY_EMPTY_EXCEPTION_HANDLER";
    }

    @Override
    public String getName() {
        return "Empty Exception Handler";
    }

    @Override
    public List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId) {
        List<Finding> findings = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return findings;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // Java / C# / JS single line empty catch: } catch (...) {} or catch (...) {}
            if (line.matches(".*catch\\s*(\\([^)]*\\))?\\s*\\{\\s*\\}.*")) {
                findings.add(createFinding(analysisId, relativePath, i + 1, line));
                continue;
            }

            // Multi-line empty catch check in Java / JS: catch (...) { \n }
            if (line.contains("catch") && line.endsWith("{")) {
                boolean isEmpty = true;
                int j = i + 1;
                for (; j < lines.size(); j++) {
                    String nextLine = lines.get(j).trim();
                    if (nextLine.equals("}") || nextLine.startsWith("}")) {
                        break;
                    }
                    if (!nextLine.isEmpty() && !nextLine.startsWith("//") && !nextLine.startsWith("/*") && !nextLine.startsWith("*")) {
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty && j < lines.size() && (lines.get(j).trim().equals("}") || lines.get(j).trim().startsWith("}"))) {
                    findings.add(createFinding(analysisId, relativePath, i + 1, line));
                }
            }

            // Python empty except check: except: pass or except Exception: pass
            if ((line.startsWith("except") || line.startsWith("except:")) && (line.endsWith(": pass") || (i + 1 < lines.size() && lines.get(i + 1).trim().equals("pass")))) {
                findings.add(createFinding(analysisId, relativePath, i + 1, line));
            }
        }

        return findings;
    }

    private Finding createFinding(String analysisId, String relativePath, int lineNum, String line) {
        Finding finding = new Finding();
        finding.setAnalysisId(analysisId);
        finding.setCategory("CODE_QUALITY");
        finding.setRuleId(getRuleId());
        finding.setSeverity("HIGH");
        finding.setTitle("Empty Exception Handler");
        finding.setDescription("An exception is caught without any error handling or logging at line " + lineNum + ". Swallowing exceptions conceals failures, causing difficult runtime bugs.");
        finding.setFilePath(relativePath);
        finding.setLineNumber(lineNum);
        finding.setEvidence("Empty catch/except block detected at line " + lineNum + ": '" + line + "'.");
        finding.setConfidence("HIGH");
        finding.setImpact("Hides runtime failures and hinders debugging.");
        finding.setStatus("OPEN");
        return finding;
    }
}
