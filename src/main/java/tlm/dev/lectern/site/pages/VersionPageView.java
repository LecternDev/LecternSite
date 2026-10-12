package tlm.dev.lectern.site.pages;

import java.util.List;

public record VersionPageView(
        PageFrame frame,
        String id,
        String kind,
        boolean unstable,
        List<FactRow> facts,
        List<ChangeSection> sections,
        String comparisonNote) {

    public VersionPageView {
        facts = List.copyOf(facts);
        sections = List.copyOf(sections);
    }
}
