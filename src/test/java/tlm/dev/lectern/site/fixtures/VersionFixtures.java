package tlm.dev.lectern.site.fixtures;

import java.time.Instant;
import java.util.List;
import tlm.dev.lectern.site.model.VersionId;
import tlm.dev.lectern.site.model.VersionKind;
import tlm.dev.lectern.site.model.VersionSummary;

public final class VersionFixtures {

    private VersionFixtures() {
    }

    public static VersionSummary release() {
        return new VersionSummary(new VersionId("1.21.5"), new VersionKind.Release(), Instant.parse("2025-03-25T14:00:00Z"));
    }

    public static VersionSummary releaseCandidate() {
        return new VersionSummary(new VersionId("1.21.5-rc1"), new VersionKind.ReleaseCandidate(), Instant.parse("2025-03-19T14:00:00Z"));
    }

    public static VersionSummary snapshot() {
        return new VersionSummary(new VersionId("25w14craftmine"), new VersionKind.Snapshot(), Instant.parse("2025-04-01T14:00:00Z"));
    }

    public static List<VersionSummary> threeVersions() {
        return List.of(release(), releaseCandidate(), snapshot());
    }

    public static VersionSummary of(String id, VersionKind kind, String releaseTime) {
        return new VersionSummary(new VersionId(id), kind, Instant.parse(releaseTime));
    }
}
