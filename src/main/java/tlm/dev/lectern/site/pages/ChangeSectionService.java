package tlm.dev.lectern.site.pages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tlm.dev.lectern.site.model.Change;
import tlm.dev.lectern.site.model.ChangeSet;
import tlm.dev.lectern.site.model.SkippedDomain;
import tlm.dev.lectern.site.model.SkippedSide;

public final class ChangeSectionService {

    private static final String REPORT_MISSING = "report_missing";
    private static final String NO_CHANGES = "No data changes.";

    private final ChangeSentenceService sentences;

    public ChangeSectionService(ChangeSentenceService sentences) {
        this.sentences = sentences;
    }

    public ChangeSection section(ChangeSet changes, String note, String baseHref) {
        Map<String, List<Change>> byDomain = new LinkedHashMap<>();
        for (Change change : changes.changes()) {
            byDomain.computeIfAbsent(change.type().domain(), domain -> new ArrayList<>()).add(change);
        }
        List<DomainBlock> blocks = new ArrayList<>();
        byDomain.forEach((domain, domainChanges) -> blocks.add(block(domain, domainChanges)));
        return new ChangeSection(
                changes.from().id().value(),
                baseHref,
                note,
                summary(changes.changes().size(), byDomain.size()),
                changes.skipped().stream().map(skipped -> warning(skipped, changes)).toList(),
                blocks);
    }

    private DomainBlock block(String domain, List<Change> changes) {
        List<ChangeLine> lines = new ArrayList<>();
        List<ChangeLine> shifts = new ArrayList<>();
        for (Change change : changes) {
            ChangeLine line = sentences.line(change);
            (line.shift() ? shifts : lines).add(line);
        }
        return new DomainBlock(DomainNames.forChanges(domain), count(changes.size(), "change", "changes"), lines, shiftParts(shifts));
    }

    private static List<SentencePart> shiftParts(List<ChangeLine> shifts) {
        if (shifts.isEmpty()) {
            return List.of();
        }
        List<SentencePart> parts = new ArrayList<>();
        parts.add(new SentencePart("Id shifts: ", false));
        for (int index = 0; index < shifts.size(); index++) {
            if (index > 0) {
                parts.add(new SentencePart("; ", false));
            }
            parts.addAll(shifts.get(index).parts());
        }
        parts.add(new SentencePart(".", false));
        return parts;
    }

    private static String summary(int changeCount, int domainCount) {
        if (changeCount == 0) {
            return NO_CHANGES;
        }
        return count(changeCount, "change", "changes") + " in " + count(domainCount, "domain", "domains") + ".";
    }

    private static String warning(SkippedDomain skipped, ChangeSet changes) {
        String domain = DomainNames.forSkipped(skipped.domain());
        if (skipped.reason().equals(REPORT_MISSING)) {
            return domain + ": not compared, the report is missing in " + sideText(skipped.side(), changes) + ".";
        }
        return domain + ": not compared (reason: " + skipped.reason() + ", side: " + skipped.side().label() + ").";
    }

    private static String sideText(SkippedSide side, ChangeSet changes) {
        return switch (side) {
            case SkippedSide.From _ -> changes.from().id().value();
            case SkippedSide.To _ -> changes.to().id().value();
            case SkippedSide.Both _ -> "both versions";
        };
    }

    private static String count(int number, String singular, String plural) {
        return number + " " + (number == 1 ? singular : plural);
    }
}
