package tlm.dev.lectern.site.pages;

import java.util.List;
import tlm.dev.lectern.site.model.SiteData;
import tlm.dev.lectern.site.model.VersionData;

public record VersionListView(PageFrame frame, List<VersionRow> rows) {

    public VersionListView {
        rows = List.copyOf(rows);
    }

    static VersionListView of(PageFrame frame, SiteData data) {
        return new VersionListView(
                frame,
                data.versions().stream().map(VersionData::sheet).map(sheet -> VersionRow.from(frame.root(), sheet)).toList());
    }
}
