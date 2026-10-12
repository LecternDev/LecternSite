package tlm.dev.lectern.site.model;

import java.util.Objects;

public record SkippedDomain(String domain, SkippedSide side, String reason) {

    public SkippedDomain {
        Objects.requireNonNull(domain, "domain");
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(reason, "reason");
    }
}
