package tlm.dev.lectern.site.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;

public record VersionSummary(VersionId id, VersionKind kind, Instant releaseTime) {

    public static final Comparator<VersionSummary> NEWEST_FIRST = Comparator
            .comparing(VersionSummary::releaseTime, Comparator.<Instant>reverseOrder())
            .thenComparing(summary -> summary.id().value());

    public VersionSummary {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(releaseTime, "releaseTime");
    }
}
