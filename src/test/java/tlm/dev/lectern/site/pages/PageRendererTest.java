package tlm.dev.lectern.site.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tlm.dev.lectern.site.fixtures.VersionFixtures;
import tlm.dev.lectern.site.model.VersionKind;

class PageRendererTest {

    private final PageRenderer renderer = PageRenderer.precompiled();

    static Stream<Arguments> kinds() {
        return Stream.of(
                Arguments.of(new VersionKind.Release(), false),
                Arguments.of(new VersionKind.ReleaseCandidate(), true),
                Arguments.of(new VersionKind.PreRelease(), true),
                Arguments.of(new VersionKind.Snapshot(), true));
    }

    @Test
    void snapshotRowCarriesTheUnstableLabel() {
        String page = renderer.versionList(VersionFixtures.threeVersions());
        assertTrue(HtmlRows.rowOf(page, "25w14craftmine").contains(HtmlRows.UNSTABLE_LABEL));
    }

    @Test
    void releaseCandidateRowCarriesTheUnstableLabel() {
        String page = renderer.versionList(VersionFixtures.threeVersions());
        assertTrue(HtmlRows.rowOf(page, "1.21.5-rc1").contains(HtmlRows.UNSTABLE_LABEL));
    }

    @Test
    void releaseRowCarriesNoUnstableLabel() {
        String page = renderer.versionList(VersionFixtures.threeVersions());
        assertFalse(HtmlRows.rowOf(page, "1.21.5").contains("unstable"));
    }

    @Test
    void onlyUnstableVersionsAreLabeled() {
        String page = renderer.versionList(VersionFixtures.threeVersions());
        assertEquals(2, HtmlRows.countOf(page, HtmlRows.UNSTABLE_LABEL));
    }

    @ParameterizedTest
    @MethodSource("kinds")
    void everyKindIsLabeledUnstableExactlyWhenItIsUnstable(VersionKind kind, boolean unstable) {
        String page = renderer.versionList(List.of(VersionFixtures.of("1.0-test", kind, "2025-01-01T00:00:00Z")));
        assertEquals(unstable, HtmlRows.rowOf(page, "1.0-test").contains(HtmlRows.UNSTABLE_LABEL));
        assertTrue(HtmlRows.rowOf(page, "1.0-test").contains("<td>" + kind.label() + "</td>"));
    }

    @Test
    void versionsAreListedNewestFirstByReleaseTime() {
        String page = renderer.versionList(VersionFixtures.threeVersions());
        int snapshot = page.indexOf("data-version=\"25w14craftmine\"");
        int release = page.indexOf("data-version=\"1.21.5\"");
        int candidate = page.indexOf("data-version=\"1.21.5-rc1\"");
        assertTrue(snapshot > 0 && snapshot < release && release < candidate);
    }

    @Test
    void releaseDateIsShownAsAnIsoDate() {
        String page = renderer.versionList(VersionFixtures.threeVersions());
        assertTrue(HtmlRows.rowOf(page, "1.21.5").contains("<time datetime=\"2025-03-25T14:00:00Z\">2025-03-25</time>"));
    }

    @Test
    void versionIdsAreEscapedAsHtml() {
        String page = renderer.versionList(List.of(VersionFixtures.of("a&b", new VersionKind.Release(), "2025-01-01T00:00:00Z")));
        assertTrue(page.contains("data-version=\"a&amp;b\""));
        assertFalse(page.contains("a&b"));
    }

    @Test
    void pageIsAnEnglishHtmlDocument() {
        String page = renderer.versionList(VersionFixtures.threeVersions());
        assertTrue(page.startsWith("<!DOCTYPE html>"));
        assertTrue(page.contains("<html lang=\"en\">"));
        assertTrue(page.contains("<h1>Minecraft Java versions</h1>"));
    }

    @Test
    void sameInputGivesTheSameBytesWithLfAndOneFinalNewline() {
        String first = renderer.versionList(VersionFixtures.threeVersions());
        String second = renderer.versionList(VersionFixtures.threeVersions());
        assertEquals(first, second);
        assertFalse(first.contains("\r"));
        assertTrue(first.endsWith("</html>\n"));
    }

    @Test
    void emptyListRendersAPageWithoutRows() {
        String page = renderer.versionList(List.of());
        assertEquals(0, HtmlRows.countOf(page, "<tr data-version="));
        assertTrue(page.contains("<h1>Minecraft Java versions</h1>"));
    }
}
