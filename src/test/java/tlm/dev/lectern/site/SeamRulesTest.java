package tlm.dev.lectern.site;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class SeamRulesTest {

    private static final List<String> OUTPUTS = List.of("pages", "api", "feed", "notify");
    private static final List<String> ALL = List.of("model", "json", "data", "pages", "api", "feed", "notify");

    private static String importing(String... imports) {
        StringBuilder source = new StringBuilder("package tlm.dev.lectern.site.x;\n\n");
        for (String imported : imports) {
            source.append("import ").append(imported).append(";\n");
        }
        return source.append("\nclass X {\n}\n").toString();
    }

    private static List<String> violations(String from, String to) {
        return SeamRules.violations(from + "/Source.java", importing("tlm.dev.lectern.site." + to + ".Thing"));
    }

    @Test
    void familyImportingAnotherFamilyIsReported() {
        assertEquals(
                List.of("pages/Page.java: pages -> data (tlm.dev.lectern.site.data.Reader)"),
                SeamRules.violations("pages/Page.java", importing("tlm.dev.lectern.site.data.Reader")));
    }

    @Test
    void outputFamiliesKnowOnlyTheModel() {
        for (String from : OUTPUTS) {
            for (String to : ALL) {
                int expected = from.equals(to) || to.equals("model") ? 0 : 1;
                assertEquals(expected, violations(from, to).size(), from + " -> " + to);
            }
        }
    }

    @Test
    void dataKnowsTheModelAndJsonOnly() {
        for (String to : ALL) {
            int expected = to.equals("data") || to.equals("model") || to.equals("json") ? 0 : 1;
            assertEquals(expected, violations("data", to).size(), "data -> " + to);
        }
    }

    @Test
    void transverseFamiliesImportNoFamily() {
        for (String from : List.of("model", "json")) {
            for (String to : ALL) {
                int expected = from.equals(to) ? 0 : 1;
                assertEquals(expected, violations(from, to).size(), from + " -> " + to);
            }
        }
    }

    @Test
    void familyImportingARootClassIsReported() {
        assertEquals(
                List.of("api/Writer.java: api -> root (tlm.dev.lectern.site.SiteGenerator)"),
                SeamRules.violations("api/Writer.java", importing("tlm.dev.lectern.site.SiteGenerator")));
    }

    @Test
    void familyImportingAStaticMemberOfARootClassIsReported() {
        String source = "import static tlm.dev.lectern.site.SiteGenerator.OUTPUT;\n";
        assertEquals(1, SeamRules.violations("feed/Writer.java", source).size());
    }

    @Test
    void rootClassMayImportEveryFamily() {
        String source = importing(
                "tlm.dev.lectern.site.model.VersionSheet",
                "tlm.dev.lectern.site.json.JsonReader",
                "tlm.dev.lectern.site.data.LecternDataReader",
                "tlm.dev.lectern.site.pages.PageWriter",
                "tlm.dev.lectern.site.api.ApiWriter");
        assertEquals(List.of(), SeamRules.violations("Composition.java", source));
    }

    @Test
    void jteInsidePagesIsAccepted() {
        String source = importing("gg.jte.TemplateEngine", "gg.jte.output.StringOutput");
        assertEquals(List.of(), SeamRules.violations("pages/Renderer.java", source));
    }

    @Test
    void jteOutsidePagesIsReported() {
        assertEquals(
                List.of("data/Reader.java: gg.jte outside pages (gg.jte.TemplateEngine)"),
                SeamRules.violations("data/Reader.java", importing("gg.jte.TemplateEngine")));
    }

    @Test
    void jacksonInsideJsonIsAccepted() {
        String source = importing("tools.jackson.core.JsonParser", "tools.jackson.core.json.JsonFactory");
        assertEquals(List.of(), SeamRules.violations("json/Reader.java", source));
    }

    @Test
    void jacksonOutsideJsonIsReported() {
        assertEquals(
                List.of("data/Reader.java: tools.jackson.core outside json (tools.jackson.core.JsonParser)"),
                SeamRules.violations("data/Reader.java", importing("tools.jackson.core.JsonParser")));
    }

    @Test
    void thirdPartyInTheRootIsReported() {
        assertEquals(1, SeamRules.violations("Composition.java", importing("gg.jte.TemplateEngine")).size());
        assertEquals(1, SeamRules.violations("Composition.java", importing("tools.jackson.core.JsonParser")).size());
    }

    @Test
    void undeclaredThirdPartyLibraryIsReported() {
        assertEquals(
                List.of("api/Writer.java: undeclared third-party import (com.fasterxml.jackson.databind.ObjectMapper)"),
                SeamRules.violations("api/Writer.java", importing("com.fasterxml.jackson.databind.ObjectMapper")));
    }

    @Test
    void jdkImportsAreAccepted() {
        String source = importing("java.util.List", "java.nio.file.Path", "javax.xml.stream.XMLStreamWriter");
        assertEquals(List.of(), SeamRules.violations("feed/Writer.java", source));
    }

    @Test
    void undeclaredFamilyDirectoryIsReported() {
        assertEquals(
                List.of("misc/Thing.java: undeclared family misc"),
                SeamRules.violations("misc/Thing.java", importing("java.util.List")));
    }

    @Test
    void importOfAnUndeclaredOwnPackageIsReported() {
        assertEquals(
                List.of("pages/Page.java: undeclared family misc (tlm.dev.lectern.site.misc.Thing)"),
                SeamRules.violations("pages/Page.java", importing("tlm.dev.lectern.site.misc.Thing")));
    }

    @Test
    void importInsideATextBlockIsIgnored() {
        String source = """
                package tlm.dev.lectern.site.pages;

                class Page {
                    String text = \"\"\"
                import tlm.dev.lectern.site.data.Reader;
                \"\"\";
                }
                """;
        assertEquals(List.of(), SeamRules.violations("pages/Page.java", source));
    }

    @Test
    void wildcardImportOfAnotherFamilyIsReported() {
        assertEquals(1, SeamRules.violations("pages/Page.java", "import tlm.dev.lectern.site.api.*;\n").size());
    }

    @Test
    void qualifiedNameOutsideAnImportIsReported() {
        String source = "class Page {\n    gg.jte.TemplateEngine engine;\n}\n";
        assertEquals(
                List.of("pages/Page.java:2: qualified name outside an import (gg.jte.)"),
                SeamRules.violations("pages/Page.java", source));
    }

    @Test
    void qualifiedOwnNameOutsideAnImportIsReported() {
        String source = "class Page {\n    tlm.dev.lectern.site.data.Reader reader;\n}\n";
        assertEquals(1, SeamRules.violations("pages/Page.java", source).size());
    }

    @Test
    void qualifiedNameInsideAStringLiteralIsIgnored() {
        String source = "class Page {\n    String text = \"gg.jte. and tools.jackson.\";\n}\n";
        assertEquals(List.of(), SeamRules.violations("pages/Page.java", source));
    }
}
