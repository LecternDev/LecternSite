package tlm.dev.lectern.site.fixtures;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import tlm.dev.lectern.site.model.SiteData;
import tlm.dev.lectern.site.model.VersionData;
import tlm.dev.lectern.site.model.VersionId;
import tlm.dev.lectern.site.model.VersionKind;
import tlm.dev.lectern.site.model.VersionSheet;

public final class VersionFixtures {

    private VersionFixtures() {
    }

    public static VersionSheet of(String id, VersionKind kind, String releaseTime) {
        return new VersionSheet(new VersionId(id), kind, Instant.parse(releaseTime), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static VersionSheet release() {
        return of("1.21.5", new VersionKind.Release(), "2025-03-25T14:00:00Z");
    }

    public static VersionSheet releaseCandidate() {
        return of("1.21.5-rc1", new VersionKind.ReleaseCandidate(), "2025-03-19T14:00:00Z");
    }

    public static VersionSheet snapshot() {
        return of("25w14craftmine", new VersionKind.Snapshot(), "2025-04-01T14:00:00Z");
    }

    public static List<VersionSheet> threeVersions() {
        return List.of(release(), releaseCandidate(), snapshot());
    }

    public static VersionData withoutChanges(VersionSheet sheet) {
        return new VersionData(sheet, Optional.empty(), Optional.empty());
    }

    public static SiteData threeVersionSite() {
        return new SiteData(threeVersions().stream().map(VersionFixtures::withoutChanges).toList());
    }
}
