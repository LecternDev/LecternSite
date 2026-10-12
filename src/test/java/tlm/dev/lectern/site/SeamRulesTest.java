package tlm.dev.lectern.site;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class SeamRulesTest {

    private static String importing(String... imports) {
        StringBuilder source = new StringBuilder("package tlm.dev.lectern.site.x;\n\n");
        for (String imported : imports) {
            source.append("import ").append(imported).append(";\n");
        }
        return source.append("\nclass X {\n}\n").toString();
    }

    @Test
    void familyImportingAnotherFamilyIsReported() {
        String source = importing("tlm.dev.lectern.site.data.Reader");
        assertEquals(
                List.of("pages/Page.java: pages -> data (tlm.dev.lectern.site.data.Reader)"),
                SeamRules.violations("pages/Page.java", source));
    }

    @Test
    void everyOutputFamilyIsIsolatedFromTheOthers() {
        List<String> families = List.of("data", "pages", "api", "feed", "notify");
        for (String from : families) {
            for (String to : families) {
                String source = importing("tlm.dev.lectern.site." + to + ".Thing");
                int expected = from.equals(to) ? 0 : 1;
                assertEquals(expected, SeamRules.violations(from + "/Source.java", source).size(), from + " -> " + to);
            }
        }
    }

    @Test
    void familyMayImportTheTransverseModel() {
        for (String from : List.of("data", "pages", "api", "feed", "notify")) {
            String source = importing("tlm.dev.lectern.site.model.VersionSummary");
            assertEquals(List.of(), SeamRules.violations(from + "/Source.java", source), from);
        }
    }

    @Test
    void transverseModelImportingAFamilyIsReported() {
        for (String forbidden : List.of("data", "pages", "api", "feed", "notify")) {
            String source = importing("tlm.dev.lectern.site." + forbidden + ".Thing");
            assertEquals(1, SeamRules.violations("model/Value.java", source).size(), forbidden);
        }
    }

    @Test
    void familyImportingARootClassIsReported() {
        String source = importing("tlm.dev.lectern.site.SiteGenerator");
        assertEquals(
                List.of("api/Writer.java: api -> root (tlm.dev.lectern.site.SiteGenerator)"),
                SeamRules.violations("api/Writer.java", source));
    }

    @Test
    void familyImportingAStaticMemberOfARootClassIsReported() {
        String source = "import static tlm.dev.lectern.site.SiteGenerator.OUTPUT;\n";
        assertEquals(1, SeamRules.violations("feed/Writer.java", source).size());
    }

    @Test
    void rootClassMayImportEveryFamily() {
        String source = importing(
                "tlm.dev.lectern.site.model.VersionSummary",
                "tlm.dev.lectern.site.data.Reader",
                "tlm.dev.lectern.site.pages.PageWriter",
                "tlm.dev.lectern.site.api.ApiWriter");
        assertEquals(List.of(), SeamRules.violations("SiteGenerator.java", source));
    }

    @Test
    void jteInsidePagesIsAccepted() {
        String source = importing("gg.jte.TemplateEngine", "gg.jte.output.StringOutput");
        assertEquals(List.of(), SeamRules.violations("pages/Renderer.java", source));
    }

    @Test
    void jteOutsidePagesIsReported() {
        String source = importing("gg.jte.TemplateEngine");
        assertEquals(
                List.of("data/Reader.java: gg.jte outside pages (gg.jte.TemplateEngine)"),
                SeamRules.violations("data/Reader.java", source));
    }

    @Test
    void jteInTheRootIsReported() {
        String source = importing("gg.jte.TemplateEngine");
        assertEquals(1, SeamRules.violations("SiteGenerator.java", source).size());
    }

    @Test
    void undeclaredThirdPartyLibraryIsReported() {
        String source = importing("com.fasterxml.jackson.databind.ObjectMapper");
        assertEquals(
                List.of("api/Writer.java: undeclared third-party import (com.fasterxml.jackson.databind.ObjectMapper)"),
                SeamRules.violations("api/Writer.java", source));
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
        String source = importing("tlm.dev.lectern.site.misc.Thing");
        assertEquals(
                List.of("pages/Page.java: undeclared family misc (tlm.dev.lectern.site.misc.Thing)"),
                SeamRules.violations("pages/Page.java", source));
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
        String source = "import tlm.dev.lectern.site.api.*;\n";
        assertEquals(1, SeamRules.violations("pages/Page.java", source).size());
    }
}
