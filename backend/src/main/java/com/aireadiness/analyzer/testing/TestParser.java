package com.aireadiness.analyzer.testing;

import com.aireadiness.analyzer.testing.model.TestMethodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestParser {

    private static final Pattern JAVA_TEST_ANNOTATION = Pattern.compile("@(Test|ParameterizedTest|RepeatedTest)");
    private static final Pattern JAVA_METHOD_SIG = Pattern.compile("(?:public|protected|private|void|static|\\s)+([a-zA-Z0-9_]+)\\s*\\(");

    private static final Pattern JS_TEST_CALL = Pattern.compile("(?:^|\\s|;)(test|it|describe|xtest|xit)(?:\\.(?:skip|only))?\\s*\\(\\s*[\"']([^\"']+)[\"']");

    private static final Pattern PY_TEST_DEF = Pattern.compile("^\\s*def\\s+(test_[a-zA-Z0-9_]+)\\s*\\(");

    private static final Pattern GO_TEST_FUNC = Pattern.compile("^func\\s+(Test[a-zA-Z0-9_]+)\\s*\\(");

    private static final Pattern CS_TEST_ATTR = Pattern.compile("\\[(Test|Fact|Theory|TestMethod)\\]");
    private static final Pattern CS_METHOD_SIG = Pattern.compile("(?:public|protected|private|void|static|async|Task|\\s)+([a-zA-Z0-9_]+)\\s*\\(");

    private static final Pattern PHP_TEST_FUNC = Pattern.compile("(?:public|protected|private|function|\\s)+(test[a-zA-Z0-9_]+)\\s*\\(");

    private static final Pattern TODO_PATTERN = Pattern.compile("(?i)\\b(TODO|FIXME|XXX|HACK)\\b");

    // Assertion patterns
    private static final Pattern JAVA_ASSERT_PATTERN = Pattern.compile("\\b(assertEquals|assertNotEquals|assertTrue|assertFalse|assertNull|assertNotNull|assertThrows|assertThat|verify|assertArrayEquals)\\b");
    private static final Pattern JS_ASSERT_PATTERN = Pattern.compile("\\b(expect|assert|toBe|toEqual|toThrow|toStrictEqual|toBeTruthy|toBeFalsy|toContain|toHaveLength)\\b");
    private static final Pattern PY_ASSERT_PATTERN = Pattern.compile("\\b(assert|pytest\\.raises|self\\.assert[a-zA-Z0-9_]*)\\b");
    private static final Pattern GO_ASSERT_PATTERN = Pattern.compile("\\bt\\.(Error|Errorf|Fail|FailNow|Fatal|Fatalf)\\b");
    private static final Pattern CS_ASSERT_PATTERN = Pattern.compile("\\b(Assert\\.[a-zA-Z0-9_]+|Should\\(\\)\\.[a-zA-Z0-9_]+)\\b");
    private static final Pattern PHP_ASSERT_PATTERN = Pattern.compile("\\$this->assert[a-zA-Z0-9_]*");

    // Skip patterns
    private static final Pattern JAVA_SKIP_PATTERN = Pattern.compile("@(Disabled|Ignore)");
    private static final Pattern JS_SKIP_PATTERN = Pattern.compile("\\b(test\\.skip|it\\.skip|describe\\.skip|xit|xtest)\\b");
    private static final Pattern PY_SKIP_PATTERN = Pattern.compile("@(pytest\\.mark\\.skip|pytest\\.mark\\.xfail|unittest\\.skip)");
    private static final Pattern GO_SKIP_PATTERN = Pattern.compile("\\bt\\.(Skip|Skipf|SkipNow)\\b");
    private static final Pattern CS_SKIP_PATTERN = Pattern.compile("\\[Ignore\\]|Skip\\s*=");
    private static final Pattern PHP_SKIP_PATTERN = Pattern.compile("markTestSkipped");

    public static List<TestMethodInfo> parseTestMethods(String fileName, List<String> lines) {
        List<TestMethodInfo> methods = new ArrayList<>();
        if (lines == null || lines.isEmpty()) {
            return methods;
        }

        String fn = fileName.toLowerCase();
        if (fn.endsWith(".java")) {
            parseJavaTestMethods(lines, methods);
        } else if (fn.endsWith(".js") || fn.endsWith(".jsx") || fn.endsWith(".ts") || fn.endsWith(".tsx")) {
            parseJsTestMethods(lines, methods);
        } else if (fn.endsWith(".py")) {
            parsePyTestMethods(lines, methods);
        } else if (fn.endsWith(".go")) {
            parseGoTestMethods(lines, methods);
        } else if (fn.endsWith(".cs")) {
            parseCsTestMethods(lines, methods);
        } else if (fn.endsWith(".php")) {
            parsePhpTestMethods(lines, methods);
        }

        return methods;
    }

    private static void parseJavaTestMethods(List<String> lines, List<TestMethodInfo> methods) {
        boolean pendingTestAnnotation = false;
        boolean pendingSkipAnnotation = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (JAVA_SKIP_PATTERN.matcher(line).find()) {
                pendingSkipAnnotation = true;
            }

            if (JAVA_TEST_ANNOTATION.matcher(line).find()) {
                pendingTestAnnotation = true;

                // Look for method signature on same or next few lines
                for (int j = i; j < Math.min(lines.size(), i + 5); j++) {
                    Matcher m = JAVA_METHOD_SIG.matcher(lines.get(j));
                    if (m.find()) {
                        String methodName = m.group(1);
                        int startLine = j + 1;
                        int endLine = findClosingBraceLine(lines, j);
                        List<String> bodyLines = lines.subList(j, endLine);
                        List<String> annotationLines = lines.subList(i, j + 1);
                        boolean isSkipped = pendingSkipAnnotation || containsPattern(annotationLines, JAVA_SKIP_PATTERN) || containsPattern(bodyLines, JAVA_SKIP_PATTERN);
                        boolean hasAssertion = containsPattern(bodyLines, JAVA_ASSERT_PATTERN);
                        boolean hasTodo = containsPattern(bodyLines, TODO_PATTERN);
                        boolean isEmpty = isBodyEmpty(bodyLines);

                        methods.add(new TestMethodInfo(methodName, startLine, endLine, bodyLines, isSkipped, isEmpty, hasAssertion, hasTodo));

                        pendingTestAnnotation = false;
                        pendingSkipAnnotation = false;
                        i = endLine - 1;
                        break;
                    }
                }
            }
        }
    }

    private static void parseJsTestMethods(List<String> lines, List<TestMethodInfo> methods) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher m = JS_TEST_CALL.matcher(line);
            if (m.find()) {
                String testName = m.group(2);
                int startLine = i + 1;
                int endLine = findClosingBraceLine(lines, i);
                List<String> bodyLines = lines.subList(i, Math.min(lines.size(), endLine));

                boolean isSkipped = line.contains(".skip") || line.startsWith("xit") || line.startsWith("xtest") || containsPattern(bodyLines, JS_SKIP_PATTERN);
                boolean hasAssertion = containsPattern(bodyLines, JS_ASSERT_PATTERN);
                boolean hasTodo = containsPattern(bodyLines, TODO_PATTERN);
                boolean isEmpty = isBodyEmpty(bodyLines);

                methods.add(new TestMethodInfo(testName, startLine, endLine, bodyLines, isSkipped, isEmpty, hasAssertion, hasTodo));
                i = Math.max(i, endLine - 1);
            }
        }
    }

    private static void parsePyTestMethods(List<String> lines, List<TestMethodInfo> methods) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher m = PY_TEST_DEF.matcher(line);
            if (m.find()) {
                String testName = m.group(1);
                int startLine = i + 1;
                int indent = getIndentation(line);

                int endLine = i + 1;
                while (endLine < lines.size()) {
                    String el = lines.get(endLine);
                    if (!el.trim().isEmpty() && getIndentation(el) <= indent && !el.trim().startsWith("#")) {
                        break;
                    }
                    endLine++;
                }

                List<String> bodyLines = lines.subList(i, endLine);

                // Check preceding line for @pytest.mark.skip
                boolean isSkipped = (i > 0 && PY_SKIP_PATTERN.matcher(lines.get(i - 1)).find()) || containsPattern(bodyLines, PY_SKIP_PATTERN);
                boolean hasAssertion = containsPattern(bodyLines, PY_ASSERT_PATTERN);
                boolean hasTodo = containsPattern(bodyLines, TODO_PATTERN);

                boolean isEmpty = true;
                for (int k = 1; k < bodyLines.size(); k++) {
                    String bl = bodyLines.get(k).trim();
                    if (!bl.isEmpty() && !bl.startsWith("#") && !bl.equals("pass") && !bl.equals("...")) {
                        isEmpty = false;
                        break;
                    }
                }

                methods.add(new TestMethodInfo(testName, startLine, endLine, bodyLines, isSkipped, isEmpty, hasAssertion, hasTodo));
                i = Math.max(i, endLine - 1);
            }
        }
    }

    private static void parseGoTestMethods(List<String> lines, List<TestMethodInfo> methods) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher m = GO_TEST_FUNC.matcher(line);
            if (m.find()) {
                String testName = m.group(1);
                int startLine = i + 1;
                int endLine = findClosingBraceLine(lines, i);
                List<String> bodyLines = lines.subList(i, Math.min(lines.size(), endLine));

                boolean isSkipped = containsPattern(bodyLines, GO_SKIP_PATTERN);
                boolean hasAssertion = containsPattern(bodyLines, GO_ASSERT_PATTERN);
                boolean hasTodo = containsPattern(bodyLines, TODO_PATTERN);
                boolean isEmpty = isBodyEmpty(bodyLines);

                methods.add(new TestMethodInfo(testName, startLine, endLine, bodyLines, isSkipped, isEmpty, hasAssertion, hasTodo));
                i = Math.max(i, endLine - 1);
            }
        }
    }

    private static void parseCsTestMethods(List<String> lines, List<TestMethodInfo> methods) {
        boolean pendingTestAttr = false;
        boolean pendingSkipAttr = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (CS_SKIP_PATTERN.matcher(line).find()) {
                pendingSkipAttr = true;
            }

            if (CS_TEST_ATTR.matcher(line).find()) {
                pendingTestAttr = true;

                for (int j = i; j < Math.min(lines.size(), i + 5); j++) {
                    Matcher m = CS_METHOD_SIG.matcher(lines.get(j));
                    if (m.find()) {
                        String methodName = m.group(1);
                        int startLine = j + 1;
                        int endLine = findClosingBraceLine(lines, j);
                        List<String> bodyLines = lines.subList(j, Math.min(lines.size(), endLine));

                        boolean isSkipped = pendingSkipAttr || containsPattern(bodyLines, CS_SKIP_PATTERN);
                        boolean hasAssertion = containsPattern(bodyLines, CS_ASSERT_PATTERN);
                        boolean hasTodo = containsPattern(bodyLines, TODO_PATTERN);
                        boolean isEmpty = isBodyEmpty(bodyLines);

                        methods.add(new TestMethodInfo(methodName, startLine, endLine, bodyLines, isSkipped, isEmpty, hasAssertion, hasTodo));
                        pendingTestAttr = false;
                        pendingSkipAttr = false;
                        i = Math.max(i, endLine - 1);
                        break;
                    }
                }
            }
        }
    }

    private static void parsePhpTestMethods(List<String> lines, List<TestMethodInfo> methods) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher m = PHP_TEST_FUNC.matcher(line);
            if (m.find()) {
                String testName = m.group(1);
                int startLine = i + 1;
                int endLine = findClosingBraceLine(lines, i);
                List<String> bodyLines = lines.subList(i, Math.min(lines.size(), endLine));

                boolean isSkipped = containsPattern(bodyLines, PHP_SKIP_PATTERN);
                boolean hasAssertion = containsPattern(bodyLines, PHP_ASSERT_PATTERN);
                boolean hasTodo = containsPattern(bodyLines, TODO_PATTERN);
                boolean isEmpty = isBodyEmpty(bodyLines);

                methods.add(new TestMethodInfo(testName, startLine, endLine, bodyLines, isSkipped, isEmpty, hasAssertion, hasTodo));
                i = Math.max(i, endLine - 1);
            }
        }
    }

    private static int findClosingBraceLine(List<String> lines, int startIdx) {
        int depth = 0;
        boolean foundOpening = false;

        for (int k = startIdx; k < lines.size(); k++) {
            String line = lines.get(k);
            for (char ch : line.toCharArray()) {
                if (ch == '{') {
                    depth++;
                    foundOpening = true;
                } else if (ch == '}') {
                    depth--;
                    if (foundOpening && depth <= 0) {
                        return k + 1;
                    }
                }
            }
            if (foundOpening && depth == 0) {
                return k + 1;
            }
        }
        return Math.min(lines.size(), startIdx + 30);
    }

    private static boolean containsPattern(List<String> lines, Pattern pattern) {
        for (String line : lines) {
            if (pattern.matcher(line).find()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBodyEmpty(List<String> bodyLines) {
        if (bodyLines == null || bodyLines.size() <= 1) {
            return true;
        }

        int meaningfulCodeCount = 0;
        for (int i = 1; i < bodyLines.size(); i++) {
            String trimmed = bodyLines.get(i).trim();
            if (trimmed.isEmpty() || trimmed.equals("{") || trimmed.equals("}") || trimmed.equals("};") || trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                continue;
            }
            meaningfulCodeCount++;
        }
        return meaningfulCodeCount == 0;
    }

    private static int getIndentation(String line) {
        int count = 0;
        for (char ch : line.toCharArray()) {
            if (ch == ' ') count++;
            else if (ch == '\t') count += 4;
            else break;
        }
        return count;
    }
}
