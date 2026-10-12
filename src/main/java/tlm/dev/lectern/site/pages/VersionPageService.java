package tlm.dev.lectern.site.pages;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import tlm.dev.lectern.site.model.ChangeSet;
import tlm.dev.lectern.site.model.GameInfo;
import tlm.dev.lectern.site.model.PackVersion;
import tlm.dev.lectern.site.model.VersionData;
import tlm.dev.lectern.site.model.VersionId;
import tlm.dev.lectern.site.model.VersionSheet;

public final class VersionPageService {

    private static final DateTimeFormatter RELEASED = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'").withZone(ZoneOffset.UTC);
    private static final String STEP_NOTE = "This is the previous version in the same line.";
    private static final String SHARED_NOTE = "This is both the previous version in the same line and the previous release.";
    private static final String RELEASE_NOTE = "This is the previous release. Use this comparison to migrate from it.";
    private static final String NO_BASE_NOTE = "This version has no comparison base, so no changes are listed.";

    private final PageFrames frames;
    private final ChangeSectionService sections;

    public VersionPageService(PageFrames frames, ChangeSectionService sections) {
        this.frames = frames;
        this.sections = sections;
    }

    public VersionPageView view(VersionData data, Set<VersionId> published) {
        VersionSheet sheet = data.sheet();
        PageFrame frame = frames.frame(sheet.id().value(), SiteUrls.VERSION_DEPTH);
        return new VersionPageView(
                frame,
                sheet.id().value(),
                sheet.kind().label(),
                sheet.kind().unstable(),
                facts(sheet, frame.root(), published),
                changeSections(data, frame.root(), published),
                NO_BASE_NOTE);
    }

    private List<FactRow> facts(VersionSheet sheet, String root, Set<VersionId> published) {
        List<FactRow> facts = new ArrayList<>();
        facts.add(FactRow.plain("Kind", sheet.kind().label()));
        facts.add(FactRow.plain("Released", RELEASED.format(sheet.releaseTime())));
        sheet.game().ifPresent(game -> gameFacts(game, facts));
        sheet.stepBase().ifPresent(base -> facts.add(baseFact("Previous version in the same line", base, root, published)));
        sheet.releaseBase().ifPresent(base -> facts.add(baseFact("Previous release", base, root, published)));
        return facts;
    }

    private static void gameFacts(GameInfo game, List<FactRow> facts) {
        game.worldVersion().ifPresent(version -> facts.add(FactRow.plain("Data version", Long.toString(version))));
        game.protocolVersion().ifPresent(version -> facts.add(FactRow.plain("Protocol version", Long.toString(version))));
        game.packVersion().ifPresent(pack -> facts.addAll(packFacts(pack)));
    }

    private static List<FactRow> packFacts(PackVersion pack) {
        return switch (pack) {
            case PackVersion.Single single ->
                    List.of(FactRow.plain("Pack version", single.version() + " (data packs and resource packs)"));
            case PackVersion.Split split -> List.of(
                    FactRow.plain("Data pack version", Integer.toString(split.data())),
                    FactRow.plain("Resource pack version", Integer.toString(split.resource())));
            case PackVersion.Versioned versioned -> List.of(
                    FactRow.plain("Data pack version", versioned.dataMajor() + "." + versioned.dataMinor()),
                    FactRow.plain("Resource pack version", versioned.resourceMajor() + "." + versioned.resourceMinor()));
        };
    }

    private static FactRow baseFact(String label, VersionId base, String root, Set<VersionId> published) {
        return new FactRow(label, base.value(), baseHref(base, root, published));
    }

    private List<ChangeSection> changeSections(VersionData data, String root, Set<VersionId> published) {
        List<ChangeSection> result = new ArrayList<>();
        Optional<ChangeSet> step = data.stepChanges();
        Optional<ChangeSet> release = data.releaseChanges();
        boolean sameBase = step.isPresent() && release.isPresent()
                && step.get().from().id().equals(release.get().from().id());
        step.ifPresent(changes -> result.add(
                sections.section(changes, sameBase ? SHARED_NOTE : STEP_NOTE, baseHref(changes.from().id(), root, published))));
        release.filter(changes -> !sameBase).ifPresent(changes -> result.add(
                sections.section(changes, RELEASE_NOTE, baseHref(changes.from().id(), root, published))));
        return result;
    }

    private static String baseHref(VersionId base, String root, Set<VersionId> published) {
        return published.contains(base) ? SiteUrls.versionLink(root, base) : "";
    }
}
