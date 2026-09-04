package com.aireadiness.analyzer.dependency;

import com.aireadiness.analyzer.dependency.model.DependencyInfo;
import com.aireadiness.analyzer.dependency.model.DependencyManifestInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Regex for Gradle declarations: e.g. implementation "group:artifact:1.0.0" or implementation('group:artifact:1.0.0')
    private static final Pattern GRADLE_DEP_PATTERN = Pattern.compile(
            "(?i)(implementation|api|testImplementation|runtimeOnly|compileOnly|classpath)\\s*\\(?\\s*[\"']([^\"']+)[\"']\\s*\\)?"
    );

    // Regex for Python requirements: package_name==1.2.3, package_name>=1.2.3, package_name
    private static final Pattern PYTHON_REQ_PATTERN = Pattern.compile(
            "^\\s*([a-zA-Z0-9_\\-\\.]+)\\s*([=><~=!]*)\\s*([a-zA-Z0-9_\\-\\.\\*]*)"
    );

    // Regex for Go mod require: require github.com/foo/bar v1.2.3 or github.com/foo/bar v1.2.3 inside require block
    private static final Pattern GO_MOD_REQUIRE_PATTERN = Pattern.compile(
            "^\\s*(?:require\\s+)?([a-zA-Z0-9_\\-\\./]+)\\s+(v[0-9a-zA-Z\\.-]+)"
    );

    public static DependencyManifestInfo parseManifest(Path path, Path workspaceDir) {
        String relativePathStr = workspaceDir.relativize(path).toString().replace('\\', '/');
        String filename = path.getFileName().toString().toLowerCase();

        DependencyManifestInfo manifestInfo = new DependencyManifestInfo();
        manifestInfo.setManifestPath(relativePathStr);

        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);

            if (filename.equals("pom.xml")) {
                parseMavenPom(manifestInfo, content);
            } else if (filename.endsWith(".gradle") || filename.endsWith(".gradle.kts")) {
                parseGradleBuild(manifestInfo, content);
            } else if (filename.equals("package.json")) {
                parseNpmPackageJson(manifestInfo, content);
            } else if (filename.equals("package-lock.json") || filename.equals("npm-shrinkwrap.json") || filename.equals("yarn.lock") || filename.equals("pnpm-lock.yaml")) {
                manifestInfo.setEcosystem("JavaScript");
                manifestInfo.setPackageManager("npm/Yarn/pnpm");
                manifestInfo.setLockfile(true);
            } else if (filename.equals("requirements.txt") || filename.equals("requirements-dev.txt")) {
                parsePythonRequirements(manifestInfo, content);
            } else if (filename.equals("pyproject.toml")) {
                parsePythonPyproject(manifestInfo, content);
            } else if (filename.equals("pipfile")) {
                parsePythonPipfile(manifestInfo, content);
            } else if (filename.equals("pipfile.lock") || filename.equals("poetry.lock")) {
                manifestInfo.setEcosystem("Python");
                manifestInfo.setPackageManager("Pipenv/Poetry");
                manifestInfo.setLockfile(true);
            } else if (filename.equals("go.mod")) {
                parseGoMod(manifestInfo, content);
            } else if (filename.equals("go.sum")) {
                manifestInfo.setEcosystem("Go");
                manifestInfo.setPackageManager("Go Modules");
                manifestInfo.setLockfile(true);
            } else if (filename.endsWith(".csproj") || filename.equalsIgnoreCase("packages.config") || filename.equalsIgnoreCase("Directory.Packages.props")) {
                parseDotNetProject(manifestInfo, content, filename);
            } else if (filename.equals("composer.json")) {
                parseComposerJson(manifestInfo, content);
            } else if (filename.equals("composer.lock")) {
                manifestInfo.setEcosystem("PHP");
                manifestInfo.setPackageManager("Composer");
                manifestInfo.setLockfile(true);
            }
        } catch (Exception e) {
            manifestInfo.setMalformed(true);
            manifestInfo.getWarnings().add("Failed to parse manifest: " + relativePathStr + " (" + e.getMessage() + ")");
        }

        return manifestInfo;
    }

    private static void parseMavenPom(DependencyManifestInfo manifest, String xmlContent) {
        manifest.setEcosystem("Java");
        manifest.setPackageManager("Maven");

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

            // Extract POM properties for static resolution
            Map<String, String> pomProperties = new HashMap<>();
            NodeList propertiesList = doc.getElementsByTagName("properties");
            if (propertiesList.getLength() > 0) {
                Node propertiesNode = propertiesList.item(0);
                NodeList propChildren = propertiesNode.getChildNodes();
                for (int i = 0; i < propChildren.getLength(); i++) {
                    Node prop = propChildren.item(i);
                    if (prop.getNodeType() == Node.ELEMENT_NODE) {
                        pomProperties.put(prop.getNodeName(), prop.getTextContent().trim());
                    }
                }
            }

            NodeList dependencyList = doc.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Node node = dependencyList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;

                    String groupId = getTagValue("groupId", elem);
                    String artifactId = getTagValue("artifactId", elem);
                    String version = getTagValue("version", elem);
                    String scopeStr = getTagValue("scope", elem);

                    if (artifactId == null || artifactId.isBlank()) continue;

                    String fullName = (groupId != null && !groupId.isBlank()) ? groupId + ":" + artifactId : artifactId;
                    String scope = "test".equalsIgnoreCase(scopeStr) ? "DEV" : "DIRECT";

                    String rawVersion = version;
                    String versionType = classifyVersion(rawVersion);

                    // If version uses property e.g. ${spring.version}, attempt static local pom resolution
                    if (rawVersion != null && rawVersion.startsWith("${") && rawVersion.endsWith("}")) {
                        String propKey = rawVersion.substring(2, rawVersion.length() - 1);
                        if (pomProperties.containsKey(propKey)) {
                            rawVersion = pomProperties.get(propKey);
                            versionType = classifyVersion(rawVersion);
                        } else {
                            versionType = "UNKNOWN"; // PROPERTY REFERENCE NOT RESOLVED LOCALLY
                        }
                    }

                    DependencyInfo info = new DependencyInfo(
                            fullName,
                            rawVersion,
                            manifest.getManifestPath(),
                            "Java",
                            scope,
                            versionType,
                            0
                    );
                    manifest.getDependencies().add(info);
                }
            }
        } catch (Exception e) {
            manifest.setMalformed(true);
            manifest.getWarnings().add("Malformed XML in pom.xml: " + e.getMessage());
        }
    }

    private static void parseGradleBuild(DependencyManifestInfo manifest, String content) {
        manifest.setEcosystem("Java");
        manifest.setPackageManager("Gradle");

        String[] lines = content.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("//") || line.startsWith("/*")) continue;

            Matcher m = GRADLE_DEP_PATTERN.matcher(line);
            if (m.find()) {
                String configuration = m.group(1);
                String notation = m.group(2);

                String scope = "testImplementation".equalsIgnoreCase(configuration) ? "DEV" : "DIRECT";
                String[] parts = notation.split(":");
                String name;
                String version = null;

                if (parts.length >= 3) {
                    name = parts[0] + ":" + parts[1];
                    version = parts[2];
                } else if (parts.length == 2) {
                    name = parts[0] + ":" + parts[1];
                } else {
                    name = notation;
                }

                String versionType = classifyVersion(version);
                DependencyInfo info = new DependencyInfo(
                        name,
                        version,
                        manifest.getManifestPath(),
                        "Java",
                        scope,
                        versionType,
                        i + 1
                );
                manifest.getDependencies().add(info);
            }
        }
    }

    private static void parseNpmPackageJson(DependencyManifestInfo manifest, String jsonContent) {
        manifest.setEcosystem("JavaScript");
        manifest.setPackageManager("npm");

        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonContent);
            if (root == null || !root.isObject()) {
                manifest.setMalformed(true);
                return;
            }

            extractNpmDeps(root.get("dependencies"), "DIRECT", manifest);
            extractNpmDeps(root.get("devDependencies"), "DEV", manifest);
            extractNpmDeps(root.get("peerDependencies"), "PEER", manifest);
            extractNpmDeps(root.get("optionalDependencies"), "OPTIONAL", manifest);
        } catch (Exception e) {
            manifest.setMalformed(true);
            manifest.getWarnings().add("Malformed JSON in package.json: " + e.getMessage());
        }
    }

    private static void extractNpmDeps(JsonNode node, String scope, DependencyManifestInfo manifest) {
        if (node == null || !node.isObject()) return;
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String pkgName = entry.getKey();
            String versionStr = entry.getValue().asText();

            String versionType = classifyVersion(versionStr);
            DependencyInfo info = new DependencyInfo(
                    pkgName,
                    versionStr,
                    manifest.getManifestPath(),
                    "JavaScript",
                    scope,
                    versionType,
                    0
            );
            manifest.getDependencies().add(info);
        }
    }

    private static void parsePythonRequirements(DependencyManifestInfo manifest, String content) {
        manifest.setEcosystem("Python");
        manifest.setPackageManager("pip");

        String[] lines = content.split("\\r?\\n");
        boolean isDev = manifest.getManifestPath().toLowerCase().contains("dev");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("-r") || line.startsWith("-e")) continue;

            Matcher m = PYTHON_REQ_PATTERN.matcher(line);
            if (m.find()) {
                String name = m.group(1);
                String op = m.group(2);
                String ver = m.group(3);

                if (name == null || name.isBlank()) continue;

                String version = (ver != null && !ver.isBlank()) ? (op + ver) : null;
                String versionType;
                if (op.equals("==")) {
                    versionType = "EXACT";
                } else if (op.equals(">=") || op.equals("*")) {
                    versionType = (op.equals("*") || ver.equals("*")) ? "BROAD_RANGE" : "RANGE";
                } else if (version == null || op.isEmpty()) {
                    versionType = "UNPINNED";
                } else {
                    versionType = "RANGE";
                }

                DependencyInfo info = new DependencyInfo(
                        name,
                        version,
                        manifest.getManifestPath(),
                        "Python",
                        isDev ? "DEV" : "DIRECT",
                        versionType,
                        i + 1
                );
                manifest.getDependencies().add(info);
            }
        }
    }

    private static void parsePythonPyproject(DependencyManifestInfo manifest, String content) {
        manifest.setEcosystem("Python");
        manifest.setPackageManager("Poetry/Flit/Setuptools");

        String[] lines = content.split("\\r?\\n");
        boolean inDependenciesSection = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("[") && line.endsWith("]")) {
                inDependenciesSection = line.equalsIgnoreCase("[project]") ||
                        line.contains("dependencies") ||
                        line.equalsIgnoreCase("[tool.poetry.dependencies]");
                continue;
            }

            if (inDependenciesSection && line.contains("=")) {
                String[] parts = line.split("=", 2);
                String name = parts[0].trim().replaceAll("[\"']", "");
                String verRaw = parts[1].trim().replaceAll("[\"']", "");

                if (name.isBlank() || name.equals("python")) continue;

                String versionType = classifyVersion(verRaw);
                DependencyInfo info = new DependencyInfo(
                        name,
                        verRaw,
                        manifest.getManifestPath(),
                        "Python",
                        "DIRECT",
                        versionType,
                        i + 1
                );
                manifest.getDependencies().add(info);
            }
        }
    }

    private static void parsePythonPipfile(DependencyManifestInfo manifest, String content) {
        manifest.setEcosystem("Python");
        manifest.setPackageManager("Pipenv");

        String[] lines = content.split("\\r?\\n");
        String currentScope = "DIRECT";
        boolean inPackages = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.equalsIgnoreCase("[packages]")) {
                inPackages = true;
                currentScope = "DIRECT";
                continue;
            } else if (line.equalsIgnoreCase("[dev-packages]")) {
                inPackages = true;
                currentScope = "DEV";
                continue;
            } else if (line.startsWith("[")) {
                inPackages = false;
                continue;
            }

            if (inPackages && line.contains("=")) {
                String[] parts = line.split("=", 2);
                String name = parts[0].trim().replaceAll("[\"']", "");
                String ver = parts[1].trim().replaceAll("[\"']", "");

                if (name.isBlank()) continue;

                String versionType = ver.equals("*") ? "UNPINNED" : classifyVersion(ver);
                DependencyInfo info = new DependencyInfo(
                        name,
                        ver,
                        manifest.getManifestPath(),
                        "Python",
                        currentScope,
                        versionType,
                        i + 1
                );
                manifest.getDependencies().add(info);
            }
        }
    }

    private static void parseGoMod(DependencyManifestInfo manifest, String content) {
        manifest.setEcosystem("Go");
        manifest.setPackageManager("Go Modules");

        String[] lines = content.split("\\r?\\n");
        boolean inRequireBlock = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("//")) continue;

            if (line.startsWith("require (")) {
                inRequireBlock = true;
                continue;
            } else if (inRequireBlock && line.equals(")")) {
                inRequireBlock = false;
                continue;
            }

            if (line.startsWith("require ") || inRequireBlock) {
                Matcher m = GO_MOD_REQUIRE_PATTERN.matcher(line);
                if (m.find()) {
                    String modName = m.group(1);
                    String ver = m.group(2);

                    DependencyInfo info = new DependencyInfo(
                            modName,
                            ver,
                            manifest.getManifestPath(),
                            "Go",
                            "DIRECT",
                            "EXACT",
                            i + 1
                    );
                    manifest.getDependencies().add(info);
                }
            }
        }
    }

    private static void parseDotNetProject(DependencyManifestInfo manifest, String xmlContent, String filename) {
        manifest.setEcosystem("C# / .NET");
        manifest.setPackageManager("NuGet");

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));

            NodeList pkgNodes = doc.getElementsByTagName("PackageReference");
            if (pkgNodes.getLength() == 0) {
                pkgNodes = doc.getElementsByTagName("package"); // for packages.config
            }

            for (int i = 0; i < pkgNodes.getLength(); i++) {
                Node n = pkgNodes.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) n;
                    String pkgName = elem.getAttribute("Include");
                    if (pkgName.isBlank()) pkgName = elem.getAttribute("id");

                    String ver = elem.getAttribute("Version");
                    if (ver.isBlank()) ver = elem.getAttribute("version");

                    if (pkgName.isBlank()) continue;

                    String versionType = classifyVersion(ver);
                    if (ver.startsWith("$(")) {
                        versionType = "UNKNOWN"; // Property reference e.g. $(PackageVersion)
                    }

                    DependencyInfo info = new DependencyInfo(
                            pkgName,
                            ver.isBlank() ? null : ver,
                            manifest.getManifestPath(),
                            "C# / .NET",
                            "DIRECT",
                            versionType,
                            0
                    );
                    manifest.getDependencies().add(info);
                }
            }
        } catch (Exception e) {
            manifest.setMalformed(true);
            manifest.getWarnings().add("Malformed XML in .NET file: " + e.getMessage());
        }
    }

    private static void parseComposerJson(DependencyManifestInfo manifest, String jsonContent) {
        manifest.setEcosystem("PHP");
        manifest.setPackageManager("Composer");

        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonContent);
            if (root == null || !root.isObject()) {
                manifest.setMalformed(true);
                return;
            }

            extractNpmDeps(root.get("require"), "DIRECT", manifest);
            extractNpmDeps(root.get("require-dev"), "DEV", manifest);
        } catch (Exception e) {
            manifest.setMalformed(true);
            manifest.getWarnings().add("Malformed JSON in composer.json: " + e.getMessage());
        }
    }

    public static String classifyVersion(String version) {
        if (version == null || version.isBlank()) {
            return "UNPINNED";
        }

        String v = version.trim();

        if (v.equals("*") || v.equalsIgnoreCase("latest") || v.equals(">=1.0.0") || v.startsWith(">=") || v.equals(">0")) {
            return "BROAD_RANGE";
        }

        if (v.startsWith("^") || v.startsWith("~") || v.contains("||") || v.contains("-") || v.contains(">") || v.contains("<")) {
            return "RANGE";
        }

        // Exact version number e.g. 1.2.3 or 1.2.3.4 or v1.2.3
        if (v.matches("^[vV]?[0-9]+(\\.[0-9]+)*.*$")) {
            return "EXACT";
        }

        return "UNKNOWN";
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node != null && node.getParentNode() == element) {
                return node.getTextContent().trim();
            }
        }
        return null;
    }
}
