package tlm.dev.lectern.site.model;

import java.util.List;
import java.util.Objects;

public record ChangeSet(VersionRef from, VersionRef to, List<SkippedDomain> skipped, List<Change> changes) {

    public ChangeSet {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        skipped = List.copyOf(skipped);
        changes = List.copyOf(changes);
    }
}
