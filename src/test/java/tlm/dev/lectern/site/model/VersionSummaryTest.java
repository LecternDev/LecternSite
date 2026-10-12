package tlm.dev.lectern.site.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import tlm.dev.lectern.site.fixtures.VersionFixtures;

class VersionSummaryTest {

    @Test
    void newestFirstOrdersByReleaseTimeAndNotByIdText() {
        List<VersionSummary> sorted = VersionFixtures.threeVersions().stream()
                .sorted(VersionSummary.NEWEST_FIRST)
                .toList();
        assertEquals(
                List.of("25w14craftmine", "1.21.5", "1.21.5-rc1"),
                sorted.stream().map(summary -> summary.id().value()).toList());
    }

    @Test
    void sameReleaseTimeFallsBackOnTheIdForAStableOrder() {
        VersionSummary first = VersionFixtures.of("b-version", new VersionKind.Snapshot(), "2025-04-01T14:00:00Z");
        VersionSummary second = VersionFixtures.of("a-version", new VersionKind.Snapshot(), "2025-04-01T14:00:00Z");
        assertEquals(
                List.of(second, first),
                List.of(first, second).stream().sorted(VersionSummary.NEWEST_FIRST).toList());
    }

    @Test
    void missingPartsAreRejected() {
        VersionId id = new VersionId("1.21.5");
        assertThrows(NullPointerException.class, () -> new VersionSummary(null, new VersionKind.Release(), Instant.EPOCH));
        assertThrows(NullPointerException.class, () -> new VersionSummary(id, null, Instant.EPOCH));
        assertThrows(NullPointerException.class, () -> new VersionSummary(id, new VersionKind.Release(), null));
    }
}
