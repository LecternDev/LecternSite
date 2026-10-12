package tlm.dev.lectern.site.pages;

import java.time.ZoneOffset;
import tlm.dev.lectern.site.model.VersionSummary;

public record VersionRow(String id, String kind, boolean unstable, String releasedAt, String releasedOn) {

    static VersionRow from(VersionSummary summary) {
        return new VersionRow(
                summary.id().value(),
                summary.kind().label(),
                summary.kind().unstable(),
                summary.releaseTime().toString(),
                summary.releaseTime().atZone(ZoneOffset.UTC).toLocalDate().toString());
    }
}
