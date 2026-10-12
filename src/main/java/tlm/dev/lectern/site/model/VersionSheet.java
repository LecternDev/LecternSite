package tlm.dev.lectern.site.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public record VersionSheet(
        VersionId id,
        VersionKind kind,
        Instant releaseTime,
        Optional<VersionId> stepBase,
        Optional<VersionId> releaseBase,
        Optional<GameInfo> game) {

    public static final Comparator<VersionSheet> NEWEST_FIRST = Comparator
            .comparing(VersionSheet::releaseTime, Comparator.<Instant>reverseOrder())
            .thenComparing(sheet -> sheet.id().value());

    public VersionSheet {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(releaseTime, "releaseTime");
        Objects.requireNonNull(stepBase, "stepBase");
        Objects.requireNonNull(releaseBase, "releaseBase");
        Objects.requireNonNull(game, "game");
    }
}
