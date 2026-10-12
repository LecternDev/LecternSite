package tlm.dev.lectern.site.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tlm.dev.lectern.site.data.ChangeSetReader;
import tlm.dev.lectern.site.data.LecternDataReader;
import tlm.dev.lectern.site.data.VersionSheetReader;
import tlm.dev.lectern.site.fixtures.VersionFixtures;
import tlm.dev.lectern.site.json.JsonReader;
import tlm.dev.lectern.site.model.ContactMethod;
import tlm.dev.lectern.site.model.SiteData;
import tlm.dev.lectern.site.model.VersionData;
import tlm.dev.lectern.site.model.VersionKind;

class PageRendererTest {

    private static final SiteData INVENTED = new LecternDataReader(
                    Path.of("src/test/resources/lectern-data"), new JsonReader(), new VersionSheetReader(), new ChangeSetReader())
            .read();
    private static final PageRenderer RENDERER = PageRenderer.precompiled();

    static Stream<Arguments> kinds() {
        return Stream.of(
                Arguments.of(new VersionKind.Release(), false),
                Arguments.of(new VersionKind.ReleaseCandidate(), true),
                Arguments.of(new VersionKind.PreRelease(), true),
                Arguments.of(new VersionKind.Snapshot(), true));
    }

    private static PageFrames frames(String contact) {
        return new PageFrames(Optional.ofNullable(contact).map(ContactMethod::new));
    }

    private static String list(SiteData data, String contact) {
        PageFrames frames = frames(contact);
        return RENDERER.versionList(VersionListView.of(frames.frame("Versions", SiteUrls.HOME_DEPTH), data));
    }

    private static String page(String id, String contact) {
        PageFrames frames = frames(contact);
        VersionPageService service = new VersionPageService(
                frames, new ChangeSectionService(new ChangeSentenceService(new ChangeValueRenderer())));
        VersionData version = INVENTED.versions().stream().filter(data -> data.sheet().id().value().equals(id)).findFirst().orElseThrow();
        return RENDERER.versionPage(service.view(version, INVENTED.ids()));
    }

    @Test
    void snapshotRowCarriesTheUnstableLabel() {
        String page = list(VersionFixtures.threeVersionSite(), null);
        assertTrue(HtmlRows.rowOf(page, "25w14craftmine").contains(HtmlRows.UNSTABLE_LABEL));
    }

    @Test
    void releaseCandidateRowCarriesTheUnstableLabel() {
        String page = list(VersionFixtures.threeVersionSite(), null);
        assertTrue(HtmlRows.rowOf(page, "1.21.5-rc1").contains(HtmlRows.UNSTABLE_LABEL));
    }

    @Test
    void releaseRowCarriesNoUnstableLabel() {
        String page = list(VersionFixtures.threeVersionSite(), null);
        assertFalse(HtmlRows.rowOf(page, "1.21.5").contains("unstable"));
    }

    @ParameterizedTest
    @MethodSource("kinds")
    void everyKindIsLabeledUnstableExactlyWhenItIsUnstable(VersionKind kind, boolean unstable) {
        SiteData data = new SiteData(List.of(VersionFixtures.withoutChanges(VersionFixtures.of("1.0-test", kind, "2025-01-01T00:00:00Z"))));
        String row = HtmlRows.rowOf(list(data, null), "1.0-test");
        assertEquals(unstable, row.contains(HtmlRows.UNSTABLE_LABEL));
        assertTrue(row.contains("<td>" + kind.label() + "</td>"));
    }

    @Test
    void versionsAreListedNewestFirstByReleaseTime() {
        String page = list(VersionFixtures.threeVersionSite(), null);
        int snapshot = page.indexOf("data-version=\"25w14craftmine\"");
        int release = page.indexOf("data-version=\"1.21.5\"");
        int candidate = page.indexOf("data-version=\"1.21.5-rc1\"");
        assertTrue(snapshot > 0 && snapshot < release && release < candidate);
    }

    @Test
    void listLinksEveryVersionWithARelativeLink() {
        String row = HtmlRows.rowOf(list(VersionFixtures.threeVersionSite(), null), "1.21.5-rc1");
        assertTrue(row.contains("<a href=\"versions/1.21.5-rc1/\">1.21.5-rc1</a>"), row);
    }

    @Test
    void releaseDateIsShownAsAnIsoDate() {
        String row = HtmlRows.rowOf(list(VersionFixtures.threeVersionSite(), null), "1.21.5");
        assertTrue(row.contains("<time datetime=\"2025-03-25T14:00:00Z\">2025-03-25</time>"), row);
    }

    @Test
    void emptySiteSaysNoVersionsArePublished() {
        String page = list(new SiteData(List.of()), null);
        assertEquals(0, HtmlRows.countOf(page, "<tr data-version="));
        assertTrue(page.contains("No versions have been published yet."));
    }

    @Test
    void versionIdsAreEscapedAsHtmlAndEncodedInLinks() {
        SiteData data = new SiteData(List.of(VersionFixtures.withoutChanges(
                VersionFixtures.of("a&b", new VersionKind.Release(), "2025-01-01T00:00:00Z"))));
        String page = list(data, null);
        assertTrue(page.contains("data-version=\"a&amp;b\""));
        assertTrue(page.contains("href=\"versions/a%26b/\""));
        assertFalse(page.contains(">a&b<"));
    }

    @ParameterizedTest
    @MethodSource("everyPage")
    void everyPageCarriesTheMojangNoticeAndTheNames(String name, String html) {
        assertEquals(1, HtmlRows.countOf(html, PageFrames.NOTICE), name);
        assertTrue(html.contains("<title>"), name);
        assertTrue(html.contains(">Lectern</a>"), name);
        assertTrue(html.contains("for Minecraft Java"), name);
        assertFalse(html.contains("<title>Minecraft"), name);
    }

    @ParameterizedTest
    @MethodSource("everyPage")
    void everyPageIsSelfContainedAndShowsNoImage(String name, String html) {
        for (String forbidden : List.of("<img", "<svg", "<picture", "<link", "<script", "src=", "url(", "@import", "@font-face")) {
            assertFalse(html.contains(forbidden), name + " contains " + forbidden);
        }
    }

    @ParameterizedTest
    @MethodSource("everyPage")
    void everyPageIsReadableOnAPhoneAndHasADarkMode(String name, String html) {
        assertTrue(html.contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"), name);
        assertTrue(html.contains("prefers-color-scheme: dark"), name);
        assertTrue(html.contains("<html lang=\"en\">"), name);
    }

    @ParameterizedTest
    @MethodSource("everyPage")
    void everyLinkIsRelativeExceptTheContact(String name, String html) {
        Matcher links = Pattern.compile("href=\"([^\"]*)\"").matcher(html);
        while (links.find()) {
            String target = links.group(1);
            boolean contact = target.startsWith("mailto:") || target.startsWith("https://");
            boolean relative = target.equals("./") || target.startsWith("../") || target.startsWith("versions/");
            assertTrue(contact || relative, name + " links to " + target);
        }
    }

    @ParameterizedTest
    @MethodSource("everyPage")
    void everyPageUsesLfAndEndsWithOneNewline(String name, String html) {
        assertFalse(html.contains("\r"), name);
        assertTrue(html.endsWith("</html>\n"), name);
    }

    static Stream<Arguments> everyPage() {
        return Stream.of(
                Arguments.of("list", list(INVENTED, "team@example.org")),
                Arguments.of("empty list", list(new SiteData(List.of()), "team@example.org")),
                Arguments.of("1.0", page("1.0", "team@example.org")),
                Arguments.of("2.0", page("2.0", "team@example.org")),
                Arguments.of("2.1-snapshot-1", page("2.1-snapshot-1", null)));
    }

    @Test
    void contactGivenAsAnEmailIsALinkAndSaysWhoIsResponsible() {
        String html = list(INVENTED, "team@example.org");
        assertTrue(html.contains("Responsible party and contact: <a href=\"mailto:team@example.org\">team@example.org</a>"), html);
        assertTrue(html.contains("Lectern is run by LecternDev."));
    }

    @Test
    void contactGivenAsAnHttpsAddressIsALink() {
        String html = list(INVENTED, "https://example.org/contact");
        assertTrue(html.contains("<a href=\"https://example.org/contact\">https://example.org/contact</a>"), html);
    }

    @Test
    void contactGivenAsPlainTextIsShownWithoutALink() {
        String html = list(INVENTED, "the contact form of the maintainers");
        assertTrue(html.contains("Responsible party and contact: the contact form of the maintainers</p>"), html);
        assertFalse(html.contains("mailto:"));
    }

    @Test
    void missingContactIsSaidPlainlyAndNothingIsInvented() {
        String html = list(INVENTED, null);
        assertTrue(html.contains("No contact method is configured: this is a local build and must not be published."));
        assertFalse(html.contains("Responsible party"));
    }

    @Test
    void contactIsEscapedAsHtml() {
        String html = list(INVENTED, "a<b>@example.org");
        assertTrue(html.contains("a&lt;b&gt;@example.org"), html);
        assertFalse(html.contains("<b>"));
    }

    @Test
    void headerNamesLecternFirstAndMinecraftJavaSecond() {
        String html = list(INVENTED, null);
        int name = html.indexOf("class=\"site-name\"");
        int tagline = html.indexOf("class=\"site-tagline\"");
        assertTrue(name > 0 && name < tagline);
        assertTrue(html.contains("<title>Versions - Lectern for Minecraft Java</title>"));
    }

    @Test
    void homeLinkIsRelativeOnEveryPage() {
        assertTrue(list(INVENTED, null).contains("class=\"site-name\" href=\"./\""));
        assertTrue(page("2.0", null).contains("class=\"site-name\" href=\"../../\""));
    }

    @Test
    void versionPageShowsTheVersionTheKindAndTheUnstableLabelOnlyForUnstableVersions() {
        String snapshot = page("2.1-snapshot-1", null);
        assertTrue(snapshot.contains("<h1>2.1-snapshot-1 <span class=\"unstable\">unstable</span></h1>"), snapshot);
        String release = page("2.0", null);
        assertTrue(release.contains("<h1>2.0</h1>"), release);
        assertFalse(release.contains(HtmlRows.UNSTABLE_LABEL));
    }

    @Test
    void versionPageShowsTheSheet() {
        String html = page("2.0", null);
        assertTrue(html.contains("<dt>Released</dt>"));
        assertTrue(html.contains("<dd>2025-02-10 09:00 UTC</dd>"));
        assertTrue(html.contains("<dt>Data version</dt>"));
        assertTrue(html.contains("<dd>3001</dd>"));
        assertTrue(html.contains("<dd>10.2</dd>"));
        assertTrue(html.contains("<dd>8.1</dd>"));
    }

    @Test
    void versionPageLinksItsBasesRelatively() {
        String html = page("2.0", null);
        assertTrue(html.contains("<a href=\"../../versions/2.0-rc-1/\">2.0-rc-1</a>"), html);
        assertTrue(html.contains("<a href=\"../../versions/1.5/\">1.5</a>"), html);
    }

    @Test
    void versionPageHasOneChangesSectionPerBase() {
        String html = page("2.0", null);
        assertEquals(2, HtmlRows.countOf(html, "<h2>Changes since "));
        assertTrue(html.contains("<h2>Changes since <a href=\"../../versions/2.0-rc-1/\">2.0-rc-1</a></h2>"), html);
        assertTrue(html.contains("<h2>Changes since <a href=\"../../versions/1.5/\">1.5</a></h2>"), html);
    }

    @Test
    void emptyDiffSaysThereAreNoDataChanges() {
        assertTrue(page("2.0", null).contains("<p>No data changes.</p>"));
    }

    @Test
    void changesAreGroupedByDomainWithTheirCounts() {
        String html = page("2.0-rc-1", null);
        assertTrue(html.contains("<h3>Registries <span class=\"count\">4 changes</span></h3>"), html);
        assertTrue(html.contains("<h3>Blocks <span class=\"count\">4 changes</span></h3>"), html);
        assertTrue(html.contains("<h3>Commands <span class=\"count\">1 change</span></h3>"), html);
        assertTrue(html.contains("<p>9 changes in 3 domains.</p>"), html);
    }

    @Test
    void knownTypeIsRenderedAsASentenceWithHighlightedValues() {
        String html = page("2.0-rc-1", null);
        assertTrue(html.contains("<li>Entry <code>demo:glossy</code> was added to registry <code>demo:gadget</code> (id <code>1</code>).</li>"), html);
    }

    @Test
    void unknownTypeIsRenderedGenericallyAndNeverHidden() {
        String html = page("2.0", null);
        assertTrue(html.contains("<h3>Recipe <span class=\"count\">1 change</span></h3>"), html);
        assertTrue(html.contains("<li>Change <code>recipe.added</code>: count <code>3</code>, recipe <code>demo:cake</code>.</li>"), html);
    }

    @Test
    void idShiftsAreOnOneLinePerDomain() {
        String html = page("2.0-rc-1", null);
        assertTrue(html.contains(
                "<p class=\"shifts\">Id shifts: registries: <code>4</code> from id <code>2</code>; "
                        + "<code>demo:gadget</code> entries: <code>12</code> from id <code>3</code>.</p>"), html);
        assertEquals(2, HtmlRows.countOf(html, "<p class=\"shifts\">"));
    }

    @Test
    void skippedDomainIsAWarning() {
        String html = page("2.0-rc-1", null);
        assertTrue(html.contains("<p class=\"warning\"><strong>Warning:</strong> Packets: not compared, the report is missing in 1.5.</p>"), html);
    }

    @Test
    void versionWithoutABaseExplainsWhyThereAreNoChanges() {
        String html = page("1.0", null);
        assertEquals(0, HtmlRows.countOf(html, "<h2>Changes since "));
        assertTrue(html.contains("This version has no comparison base, so no changes are listed."));
    }

    @Test
    void sameInputGivesTheSameBytes() {
        assertEquals(page("2.0", "team@example.org"), page("2.0", "team@example.org"));
        assertEquals(list(INVENTED, null), list(INVENTED, null));
    }
}
