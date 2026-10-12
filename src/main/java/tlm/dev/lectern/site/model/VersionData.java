package tlm.dev.lectern.site.model;

import java.util.Objects;
import java.util.Optional;

public record VersionData(VersionSheet sheet, Optional<ChangeSet> stepChanges, Optional<ChangeSet> releaseChanges) {

    public VersionData {
        Objects.requireNonNull(sheet, "sheet");
        Objects.requireNonNull(stepChanges, "stepChanges");
        Objects.requireNonNull(releaseChanges, "releaseChanges");
    }
}
