package tlm.dev.lectern.site.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tlm.dev.lectern.site.fixtures.VersionFixtures;

class VersionSheetTest {

    @Test
    void newestFirstOrdersByReleaseTimeAndNotByIdText() {
        List<VersionSheet> sorted = VersionFixtures.threeVersions().stream()
                .sorted(VersionSheet.NEWEST_FIRST)
                .toList();
        assertEquals(
                List.of("25w14craftmine", "1.21.5", "1.21.5-rc1"),
                sorted.stream().map(sheet -> sheet.id().value()).toList());
    }

    @Test
    void sameReleaseTimeFallsBackOnTheIdForAStableOrder() {
        VersionSheet first = VersionFixtures.of("b-version", new VersionKind.Snapshot(), "2025-04-01T14:00:00Z");
        VersionSheet second = VersionFixtures.of("a-version", new VersionKind.Snapshot(), "2025-04-01T14:00:00Z");
        assertEquals(
                List.of(second, first),
                List.of(first, second).stream().sorted(VersionSheet.NEWEST_FIRST).toList());
    }

    @Test
    void siteDataKeepsTheVersionsNewestFirst() {
        SiteData data = VersionFixtures.threeVersionSite();
        assertEquals(
                List.of("25w14craftmine", "1.21.5", "1.21.5-rc1"),
                data.versions().stream().map(version -> version.sheet().id().value()).toList());
    }

    @Test
    void siteDataListsTheIdsItHolds() {
        assertEquals(
                Set.of(new VersionId("1.21.5"), new VersionId("1.21.5-rc1"), new VersionId("25w14craftmine")),
                VersionFixtures.threeVersionSite().ids());
    }

    @Test
    void missingPartsAreRejected() {
        VersionId id = new VersionId("1.21.5");
        VersionKind kind = new VersionKind.Release();
        Optional<VersionId> none = Optional.empty();
        assertThrows(NullPointerException.class, () -> new VersionSheet(null, kind, Instant.EPOCH, none, none, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new VersionSheet(id, null, Instant.EPOCH, none, none, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new VersionSheet(id, kind, null, none, none, Optional.empty()));
    }
}
