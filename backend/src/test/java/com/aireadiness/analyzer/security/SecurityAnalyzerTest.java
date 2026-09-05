package com.aireadiness.analyzer.security;

import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;
import com.aireadiness.model.SecuritySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAnalyzerTest {

    private SecurityAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SecurityAnalyzer(2, 50, 5000, 2000);
    }

    @Test
    void testHardcodedPasswordDetection(@TempDir Path tempDir) throws IOException {
        String code = "String password = \"supersecret12345\";";
        Files.writeString(tempDir.resolve("App.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_HARDCODED_SECRET".equals(f.getRuleId())));
        Finding secretFinding = findings.stream().filter(f -> "SECURITY_HARDCODED_SECRET".equals(f.getRuleId())).findFirst().get();
        assertFalse(secretFinding.getEvidence().contains("supersecret12345"));
        assertTrue(secretFinding.getEvidence().contains("[REDACTED SECRET]"));
    }

    @Test
    void testPrivateKeyDetection(@TempDir Path tempDir) throws IOException {
        String keyFile = "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEA0...\n-----END RSA PRIVATE KEY-----";
        Files.writeString(tempDir.resolve("private.pem"), keyFile);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        Finding keyFinding = findings.stream().filter(f -> "SECURITY_HARDCODED_SECRET".equals(f.getRuleId())).findFirst().orElseThrow();
        assertEquals("CRITICAL", keyFinding.getSeverity());
    }

    @Test
    void testPlaceholderExclusion(@TempDir Path tempDir) throws IOException {
        String code = "String password = \"your-password-here\"; String api_key = \"XXXXX\";";
        Files.writeString(tempDir.resolve("App.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertFalse(findings.stream().anyMatch(f -> "SECURITY_HARDCODED_SECRET".equals(f.getRuleId())));
    }

    @Test
    void testInsecureHttpDetection(@TempDir Path tempDir) throws IOException {
        String code = "String apiUrl = \"http://api.unencrypted-service.com/data\";";
        Files.writeString(tempDir.resolve("App.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_INSECURE_HTTP".equals(f.getRuleId())));
    }

    @Test
    void testLocalhostHttpExclusion(@TempDir Path tempDir) throws IOException {
        String code = "String local = \"http://localhost:8080\"; String loopback = \"http://127.0.0.1:3000\";";
        Files.writeString(tempDir.resolve("App.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertFalse(findings.stream().anyMatch(f -> "SECURITY_INSECURE_HTTP".equals(f.getRuleId())));
    }

    @Test
    void testTlsVerificationDisabled(@TempDir Path tempDir) throws IOException {
        String code = "axios.get('https://api.com', { rejectUnauthorized: false });";
        Files.writeString(tempDir.resolve("api.js"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_TLS_VERIFICATION_DISABLED".equals(f.getRuleId())));
    }

    @Test
    void testRuntimeExecDetection(@TempDir Path tempDir) throws IOException {
        String code = "Runtime.getRuntime().exec(\"ls -l\");";
        Files.writeString(tempDir.resolve("Exec.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_DANGEROUS_EXECUTION".equals(f.getRuleId())));
    }

    @Test
    void testProcessBuilderDetection(@TempDir Path tempDir) throws IOException {
        String code = "ProcessBuilder pb = new ProcessBuilder(\"bash\", \"-c\", cmd);";
        Files.writeString(tempDir.resolve("Proc.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_DANGEROUS_EXECUTION".equals(f.getRuleId())));
    }

    @Test
    void testJsEvalDetection(@TempDir Path tempDir) throws IOException {
        String code = "eval(req.query.code);";
        Files.writeString(tempDir.resolve("server.js"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_DANGEROUS_EXECUTION".equals(f.getRuleId())));
    }

    @Test
    void testPythonEvalExecDetection(@TempDir Path tempDir) throws IOException {
        String code = "import os\nos.system(\"rm -rf \" + path)";
        Files.writeString(tempDir.resolve("script.py"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_DANGEROUS_EXECUTION".equals(f.getRuleId())));
    }

    @Test
    void testSqlInjectionConcatenation(@TempDir Path tempDir) throws IOException {
        String code = "String sql = \"SELECT * FROM users WHERE username = '\" + userInput + \"'\";";
        Files.writeString(tempDir.resolve("Dao.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_SQL_INJECTION_RISK".equals(f.getRuleId())));
    }

    @Test
    void testParameterizedSqlExclusion(@TempDir Path tempDir) throws IOException {
        String code = "String sql = \"SELECT * FROM users WHERE username = ?\";";
        Files.writeString(tempDir.resolve("Dao.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertFalse(findings.stream().anyMatch(f -> "SECURITY_SQL_INJECTION_RISK".equals(f.getRuleId())));
    }

    @Test
    void testCommandInjection(@TempDir Path tempDir) throws IOException {
        String code = "String cmd = \"ping \" + hostAddress;";
        Files.writeString(tempDir.resolve("Ping.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_COMMAND_INJECTION_RISK".equals(f.getRuleId())));
    }

    @Test
    void testPathTraversal(@TempDir Path tempDir) throws IOException {
        String code = "File f = new File(baseDir + \"/\" + request.getParameter(\"file\"));";
        Files.writeString(tempDir.resolve("Download.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_PATH_TRAVERSAL_RISK".equals(f.getRuleId())));
    }

    @Test
    void testInsecureDeserializationJava(@TempDir Path tempDir) throws IOException {
        String code = "ObjectInputStream ois = new ObjectInputStream(in);\nObject obj = ois.readObject();";
        Files.writeString(tempDir.resolve("Ser.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_INSECURE_DESERIALIZATION".equals(f.getRuleId())));
    }

    @Test
    void testWeakCryptographyMd5Sha1(@TempDir Path tempDir) throws IOException {
        String code = "MessageDigest md = MessageDigest.getInstance(\"MD5\");";
        Files.writeString(tempDir.resolve("Hash.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_WEAK_CRYPTOGRAPHY".equals(f.getRuleId())));
    }

    @Test
    void testDebugEnabled(@TempDir Path tempDir) throws IOException {
        String code = "DEBUG = True";
        Files.writeString(tempDir.resolve("settings.py"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_DEBUG_ENABLED".equals(f.getRuleId())));
    }

    @Test
    void testPermissiveCors(@TempDir Path tempDir) throws IOException {
        String code = "res.setHeader('Access-Control-Allow-Origin', '*');";
        Files.writeString(tempDir.resolve("server.js"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_PERMISSIVE_CORS".equals(f.getRuleId())));
    }

    @Test
    void testSensitiveEnvFileWithSecret(@TempDir Path tempDir) throws IOException {
        String envContent = "DB_PASS=live_db_secret_key_998877\nPORT=8080";
        Files.writeString(tempDir.resolve(".env"), envContent);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_ENV_FILE_WITH_SECRET".equals(f.getRuleId())));
    }

    @Test
    void testSensitiveKeyFile(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("service-account.json"), "{\"type\": \"service_account\"}");

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "SECURITY_SENSITIVE_FILE_EXPOSED".equals(f.getRuleId())));
    }

    @Test
    void testSelectedContentSemantics(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("App.java"), "public class App {}");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-sel", "SELECTED_CONTENT", warnings);

        SecuritySummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertTrue(summary.getSecurityWarnings().stream().anyMatch(w -> w.contains("selected content")));
        assertEquals("PARTIAL", summary.getSecurityCompleteness());
    }

    @Test
    void testUnsupportedProjectHandling(@TempDir Path tempDir) throws IOException {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("UNKNOWN");
        profile.setProjectType("UNKNOWN");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-unsupported", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.isEmpty());
        assertEquals("UNKNOWN", analyzer.getLastSummary().getSecurityCompleteness());
    }

    @Test
    void testSecretRedactionNeverExposesSecrets(@TempDir Path tempDir) throws IOException {
        String code = "String secret = \"super_sensitive_api_token_value_abc123\";";
        Files.writeString(tempDir.resolve("Config.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        for (Finding f : findings) {
            if (f.getEvidence() != null) {
                assertFalse(f.getEvidence().contains("super_sensitive_api_token_value_abc123"));
            }
            if (f.getDescription() != null) {
                assertFalse(f.getDescription().contains("super_sensitive_api_token_value_abc123"));
            }
        }
    }

    @Test
    void testStaticSecurityNoProcessExecution() {
        String className = SecurityAnalyzer.class.getName();
        assertNotNull(className);
        assertFalse(className.contains("ProcessBuilder"));
        assertFalse(className.contains("Runtime.exec"));
    }

    private List<Finding> analyze(Path workspaceDir, String uploadMode) {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        profile.setProjectType("BACKEND");
        List<String> warnings = new ArrayList<>();
        return analyzer.analyze(workspaceDir, profile, "analysis-test-id", uploadMode, warnings);
    }
}
