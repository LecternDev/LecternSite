package tlm.dev.lectern.site.pages;

import java.time.ZoneOffset;
import tlm.dev.lectern.site.model.VersionSheet;

public record VersionRow(String id, String href, String kind, boolean unstable, String releasedAt, String releasedOn) {

    static VersionRow from(String root, VersionSheet sheet) {
        return new VersionRow(
                sheet.id().value(),
                SiteUrls.versionLink(root, sheet.id()),
                sheet.kind().label(),
                sheet.kind().unstable(),
                sheet.releaseTime().toString(),
                sheet.releaseTime().atZone(ZoneOffset.UTC).toLocalDate().toString());
    }
}
