package tlm.dev.lectern.site.model;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record SiteData(List<VersionData> versions) {

    public SiteData {
        versions = versions.stream()
                .sorted(Comparator.comparing(VersionData::sheet, VersionSheet.NEWEST_FIRST))
                .toList();
    }

    public Set<VersionId> ids() {
        return versions.stream().map(version -> version.sheet().id()).collect(Collectors.toUnmodifiableSet());
    }
}
