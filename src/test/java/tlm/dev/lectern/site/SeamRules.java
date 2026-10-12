package tlm.dev.lectern.site;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SeamRules {

    static final String ROOT = "tlm.dev.lectern.site.";

    private static final Pattern IMPORT = Pattern.compile("^\\s*import\\s+(?:static\\s+)?([\\w.]+)", Pattern.MULTILINE);
    private static final List<String> JDK_PREFIXES = List.of("java.", "javax.");
    private static final List<String> QUALIFIED_PREFIXES = List.of(ROOT, "tools.jackson.", "gg.jte.");
    private static final Set<String> FAMILIES = Set.of("model", "json", "data", "pages", "api", "feed", "notify");
    private static final Map<String, String> THIRD_PARTY_OWNERS = Map.of("gg.jte", "pages", "tools.jackson.core", "json");

    private SeamRules() {
    }

    static List<String> violations(String relativePath, String source) {
        String family = familyOf(relativePath);
        String code = SourceScanner.scan(source).code();
        List<String> found = new ArrayList<>();
        if (!family.isEmpty() && !FAMILIES.contains(family)) {
            found.add(relativePath + ": undeclared family " + family);
        }
        for (String imported : imports(code)) {
            checkImport(relativePath, family, imported, found);
        }
        found.addAll(qualifiedNames(relativePath, code));
        return found;
    }

    private static Set<String> allowedImports(String family) {
        return switch (family) {
            case "model", "json" -> Set.of();
            case "data" -> Set.of("model", "json");
            default -> Set.of("model");
        };
    }

    private static Set<String> forbiddenImports(String family) {
        if (!FAMILIES.contains(family)) {
            return Set.of();
        }
        Set<String> forbidden = new TreeSet<>(FAMILIES);
        forbidden.remove(family);
        forbidden.removeAll(allowedImports(family));
        return forbidden;
    }

    private static String familyOf(String relativePath) {
        int separator = relativePath.indexOf('/');
        return separator < 0 ? "" : relativePath.substring(0, separator);
    }

    private static List<String> imports(String code) {
        List<String> imports = new ArrayList<>();
        Matcher matcher = IMPORT.matcher(code);
        while (matcher.find()) {
            imports.add(matcher.group(1));
        }
        return imports;
    }

    private static List<String> qualifiedNames(String relativePath, String code) {
        List<String> found = new ArrayList<>();
        String[] lines = code.split("\n", -1);
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index].strip();
            if (line.startsWith("import ") || line.startsWith("package ")) {
                continue;
            }
            for (String prefix : QUALIFIED_PREFIXES) {
                if (line.contains(prefix)) {
                    found.add(relativePath + ":" + (index + 1) + ": qualified name outside an import (" + prefix + ")");
                }
            }
        }
        return found;
    }

    private static void checkImport(String relativePath, String family, String imported, List<String> found) {
        if (JDK_PREFIXES.stream().anyMatch(imported::startsWith)) {
            return;
        }
        if (imported.startsWith(ROOT)) {
            checkOwnImport(relativePath, family, imported, found);
        } else {
            checkThirdPartyImport(relativePath, family, imported, found);
        }
    }

    private static void checkOwnImport(String relativePath, String family, String imported, List<String> found) {
        String remainder = imported.substring(ROOT.length());
        int dot = remainder.indexOf('.');
        String first = dot < 0 ? remainder : remainder.substring(0, dot);
        if (!first.isEmpty() && Character.isUpperCase(first.charAt(0))) {
            if (!family.isEmpty()) {
                found.add(relativePath + ": " + family + " -> root (" + imported + ")");
            }
            return;
        }
        if (!FAMILIES.contains(first)) {
            found.add(relativePath + ": undeclared family " + first + " (" + imported + ")");
            return;
        }
        if (forbiddenImports(family).contains(first)) {
            found.add(relativePath + ": " + family + " -> " + first + " (" + imported + ")");
        }
    }

    private static void checkThirdPartyImport(String relativePath, String family, String imported, List<String> found) {
        for (Map.Entry<String, String> owner : THIRD_PARTY_OWNERS.entrySet()) {
            if (imported.startsWith(owner.getKey() + ".")) {
                if (!owner.getValue().equals(family)) {
                    found.add(relativePath + ": " + owner.getKey() + " outside " + owner.getValue() + " (" + imported + ")");
                }
                return;
            }
        }
        found.add(relativePath + ": undeclared third-party import (" + imported + ")");
    }
}
