package tlm.dev.lectern.site.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tlm.dev.lectern.site.data.ChangeSetReader;
import tlm.dev.lectern.site.data.LecternDataReader;
import tlm.dev.lectern.site.data.VersionSheetReader;
import tlm.dev.lectern.site.json.JsonReader;
import tlm.dev.lectern.site.model.SiteData;
import tlm.dev.lectern.site.model.VersionData;
import tlm.dev.lectern.site.model.VersionId;

class VersionPageServiceTest {

    private static final SiteData INVENTED = new LecternDataReader(
                    Path.of("src/test/resources/lectern-data"), new JsonReader(), new VersionSheetReader(), new ChangeSetReader())
            .read();

    private final VersionPageService service = new VersionPageService(
            new PageFrames(Optional.empty()),
            new ChangeSectionService(new ChangeSentenceService(new ChangeValueRenderer())));

    private static VersionData version(String id) {
        return INVENTED.versions().stream()
                .filter(version -> version.sheet().id().value().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private VersionPageView view(String id) {
        return service.view(version(id), INVENTED.ids());
    }

    private static List<String> labels(VersionPageView view) {
        return view.facts().stream().map(FactRow::label).toList();
    }

    private static String text(VersionPageView view, String label) {
        return view.facts().stream().filter(fact -> fact.label().equals(label)).findFirst().orElseThrow().text();
    }

    @Test
    void pageIsTitledWithTheVersionIdAndSitsTwoLevelsDeep() {
        VersionPageView view = view("2.0");
        assertEquals("2.0", view.frame().title());
        assertEquals("../../", view.frame().root());
        assertEquals("2.0", view.id());
        assertEquals("release", view.kind());
    }

    @Test
    void releaseIsNotUnstableAndEveryOtherKindIs() {
        assertFalse(view("2.0").unstable());
        assertTrue(view("2.0-rc-1").unstable());
        assertTrue(view("2.1-snapshot-1").unstable());
    }

    @Test
    void sheetShowsDateDataVersionPackVersionsAndBases() {
        VersionPageView view = view("2.0");
        assertEquals(
                List.of("Kind", "Released", "Data version", "Data pack version", "Resource pack version",
                        "Previous version in the same line", "Previous release"),
                labels(view));
        assertEquals("2025-02-10 09:00 UTC", text(view, "Released"));
        assertEquals("3001", text(view, "Data version"));
        assertEquals("10.2", text(view, "Data pack version"));
        assertEquals("8.1", text(view, "Resource pack version"));
    }

    @Test
    void protocolVersionIsShownWhenTheSheetHasIt() {
        VersionPageView view = view("2.0-rc-1");
        assertEquals("1073741900", text(view, "Protocol version"));
    }

    @Test
    void packVersionGivenAsAnIntegerIsShownOnce() {
        VersionPageView view = view("1.0");
        assertEquals("4 (data packs and resource packs)", text(view, "Pack version"));
        assertFalse(labels(view).contains("Data pack version"));
    }

    @Test
    void packVersionGivenAsDataAndResourceIsShownAsTwoRows() {
        VersionPageView view = view("1.5");
        assertEquals("26", text(view, "Data pack version"));
        assertEquals("22", text(view, "Resource pack version"));
    }

    @Test
    void sheetWithoutGameShowsOnlyKindDateAndBases() {
        assertEquals(List.of("Kind", "Released", "Previous version in the same line"), labels(view("2.1-snapshot-1")));
    }

    @Test
    void sheetWithoutBasesShowsNoBaseRows() {
        assertEquals(List.of("Kind", "Released", "Data version", "Protocol version", "Pack version"), labels(view("1.0")));
    }

    @Test
    void basesThatHaveAPageAreLinkedRelativeToTheRoot() {
        VersionPageView view = view("2.0");
        FactRow step = view.facts().stream().filter(fact -> fact.label().equals("Previous version in the same line")).findFirst().orElseThrow();
        FactRow release = view.facts().stream().filter(fact -> fact.label().equals("Previous release")).findFirst().orElseThrow();
        assertEquals("2.0-rc-1", step.text());
        assertEquals("../../versions/2.0-rc-1/", step.href());
        assertEquals("1.5", release.text());
        assertEquals("../../versions/1.5/", release.href());
    }

    @Test
    void basesWithoutAPageAreShownWithoutALink() {
        VersionPageView view = service.view(version("2.0"), Set.of(new VersionId("2.0")));
        assertTrue(view.facts().stream().filter(fact -> fact.label().startsWith("Previous")).noneMatch(FactRow::hasLink));
        assertEquals(1, view.facts().stream().filter(fact -> fact.label().equals("Previous release")).count());
        assertFalse(view.sections().getFirst().hasBaseLink());
    }

    @Test
    void releaseWithTwoDifferentBasesGetsTwoSections() {
        VersionPageView view = view("2.0");
        assertEquals(2, view.sections().size());
        ChangeSection step = view.sections().get(0);
        ChangeSection release = view.sections().get(1);
        assertEquals("2.0-rc-1", step.baseId());
        assertEquals("No data changes.", step.summary());
        assertEquals("This is the previous version in the same line.", step.note());
        assertEquals("1.5", release.baseId());
        assertEquals("7 changes in 5 domains.", release.summary());
        assertEquals("This is the previous release. Use this comparison to migrate from it.", release.note());
    }

    @Test
    void sectionsLinkTheirBaseWhenItHasAPage() {
        VersionPageView view = view("2.0");
        assertEquals("../../versions/2.0-rc-1/", view.sections().get(0).baseHref());
        assertEquals("../../versions/1.5/", view.sections().get(1).baseHref());
    }

    @Test
    void sameBaseForBothDiffsGivesASingleSection() {
        VersionData data = version("2.0");
        VersionData sameBase = new VersionData(data.sheet(), data.releaseChanges(), data.releaseChanges());
        VersionPageView view = service.view(sameBase, INVENTED.ids());
        assertEquals(1, view.sections().size());
        assertEquals("This is both the previous version in the same line and the previous release.", view.sections().getFirst().note());
    }

    @Test
    void snapshotGetsOneStepSectionWithItsCounts() {
        VersionPageView view = view("2.1-snapshot-1");
        assertEquals(1, view.sections().size());
        assertEquals("5 changes in 4 domains.", view.sections().getFirst().summary());
    }

    @Test
    void skippedDomainsBecomeWarningsOfTheirSection() {
        ChangeSection section = view("2.0-rc-1").sections().getFirst();
        assertEquals(List.of("Packets: not compared, the report is missing in 1.5."), section.warnings());
    }

    @Test
    void versionWithoutAnyDiffExplainsItHasNoBase() {
        VersionPageView view = view("1.0");
        assertEquals(List.of(), view.sections());
        assertEquals("This version has no comparison base, so no changes are listed.", view.comparisonNote());
    }
}
