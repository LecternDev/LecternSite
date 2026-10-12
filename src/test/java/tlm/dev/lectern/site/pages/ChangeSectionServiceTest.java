package tlm.dev.lectern.site.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tlm.dev.lectern.site.fixtures.ChangeFixtures.change;
import static tlm.dev.lectern.site.fixtures.ChangeFixtures.changeSet;

import java.util.List;
import org.junit.jupiter.api.Test;
import tlm.dev.lectern.site.model.Change;
import tlm.dev.lectern.site.model.SkippedDomain;
import tlm.dev.lectern.site.model.SkippedSide;

class ChangeSectionServiceTest {

    private final ChangeSectionService service =
            new ChangeSectionService(new ChangeSentenceService(new ChangeValueRenderer()));

    private ChangeSection section(List<SkippedDomain> skipped, Change... changes) {
        return service.section(changeSet("1.0", "2.0", skipped, List.of(changes)), "A note.", "../../versions/1.0/");
    }

    @Test
    void emptyChangeSetSaysThereAreNoDataChanges() {
        ChangeSection section = section(List.of());
        assertEquals("No data changes.", section.summary());
        assertEquals(List.of(), section.domains());
    }

    @Test
    void baseAndNoteAreCarriedOver() {
        ChangeSection section = section(List.of());
        assertEquals("1.0", section.baseId());
        assertEquals("../../versions/1.0/", section.baseHref());
        assertEquals("A note.", section.note());
        assertTrue(section.hasBaseLink());
    }

    @Test
    void baseWithoutAPageHasNoLink() {
        ChangeSection section = service.section(changeSet("1.0", "2.0", List.of(), List.of()), "A note.", "");
        assertEquals(false, section.hasBaseLink());
    }

    @Test
    void changesAreGroupedByDomainInOrderOfAppearanceWithCounts() {
        ChangeSection section = section(
                List.of(),
                change("registry.entry_added", "registry", "demo:gadget", "entry", "demo:a", "protocol_id", 1),
                change("registry.entry_added", "registry", "demo:gadget", "entry", "demo:b", "protocol_id", 2),
                change("block.added", "block", "demo:lamp", "state_count", 2),
                change("command.node_added", "path", List.of("demo"), "kind", "literal", "node_count", 1));
        assertEquals(List.of("Registries", "Blocks", "Commands"), section.domains().stream().map(DomainBlock::name).toList());
        assertEquals(List.of("2 changes", "1 change", "1 change"), section.domains().stream().map(DomainBlock::countText).toList());
        assertEquals("4 changes in 3 domains.", section.summary());
    }

    @Test
    void singleChangeInSingleDomainIsWrittenInTheSingular() {
        assertEquals("1 change in 1 domain.", section(List.of(), change("block.added", "block", "demo:lamp", "state_count", 2)).summary());
    }

    @Test
    void idShiftsAreOnOneLineAfterTheOtherChanges() {
        ChangeSection section = section(
                List.of(),
                change("registry.ids_shifted", "count", 4, "first_id", 2),
                change("registry.entry_added", "registry", "demo:gadget", "entry", "demo:a", "protocol_id", 1),
                change("registry.entry_ids_shifted", "registry", "demo:gadget", "count", 12, "first_id", 3));
        DomainBlock registries = section.domains().getFirst();
        assertEquals("3 changes", registries.countText());
        assertEquals(1, registries.lines().size());
        assertTrue(registries.hasShifts());
        String shifts = registries.shifts().stream().map(SentencePart::text).reduce("", String::concat);
        assertEquals("Id shifts: registries: 4 from id 2; demo:gadget entries: 12 from id 3.", shifts);
    }

    @Test
    void domainWithoutShiftsHasNoShiftLine() {
        DomainBlock blocks = section(List.of(), change("block.added", "block", "demo:lamp", "state_count", 2)).domains().getFirst();
        assertEquals(false, blocks.hasShifts());
        assertEquals(List.of(), blocks.shifts());
    }

    @Test
    void domainOfAnUnknownPrefixGetsACapitalizedHeadingAndIsNotHidden() {
        ChangeSection section = section(List.of(), change("recipe.added", "recipe", "demo:cake"));
        DomainBlock block = section.domains().getFirst();
        assertEquals("Recipe", block.name());
        assertEquals(1, block.lines().size());
        assertTrue(block.lines().getFirst().plainText().contains("recipe.added"));
    }

    @Test
    void skippedDomainMissingInTheOlderVersionNamesIt() {
        ChangeSection section = section(List.of(new SkippedDomain("packets", new SkippedSide.From(), "report_missing")));
        assertEquals(List.of("Packets: not compared, the report is missing in 1.0."), section.warnings());
    }

    @Test
    void skippedDomainMissingInTheNewerVersionNamesIt() {
        ChangeSection section = section(List.of(new SkippedDomain("commands", new SkippedSide.To(), "report_missing")));
        assertEquals(List.of("Commands: not compared, the report is missing in 2.0."), section.warnings());
    }

    @Test
    void skippedDomainMissingInBothVersionsSaysSo() {
        ChangeSection section = section(List.of(new SkippedDomain("blocks", new SkippedSide.Both(), "report_missing")));
        assertEquals(List.of("Blocks: not compared, the report is missing in both versions."), section.warnings());
    }

    @Test
    void skippedDomainWithAnotherReasonIsStillReported() {
        ChangeSection section = section(List.of(new SkippedDomain("worldgen", new SkippedSide.To(), "unreadable")));
        assertEquals(List.of("Worldgen: not compared (reason: unreadable, side: to)."), section.warnings());
    }

    @Test
    void skippedDomainsDoNotCountAsChanges() {
        ChangeSection section = section(List.of(new SkippedDomain("packets", new SkippedSide.From(), "report_missing")));
        assertEquals("No data changes.", section.summary());
    }
}
