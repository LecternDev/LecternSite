package tlm.dev.lectern.site.pages;

import java.util.List;

public record ChangeSection(
        String baseId,
        String baseHref,
        String note,
        String summary,
        List<String> warnings,
        List<DomainBlock> domains) {

    public ChangeSection {
        warnings = List.copyOf(warnings);
        domains = List.copyOf(domains);
    }

    public boolean hasBaseLink() {
        return !baseHref.isEmpty();
    }
}
