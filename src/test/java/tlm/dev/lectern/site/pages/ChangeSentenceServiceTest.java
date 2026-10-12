package tlm.dev.lectern.site.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tlm.dev.lectern.site.fixtures.ChangeFixtures.change;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tlm.dev.lectern.site.model.Change;

class ChangeSentenceServiceTest {

    private final ChangeSentenceService sentences = new ChangeSentenceService(new ChangeValueRenderer());

    static Stream<String> contractTypes() {
        return ChangeTypeCatalog.KEYS.keySet().stream().sorted();
    }

    private static Change withEveryKey(String type) {
        List<Object> keysAndValues = new ArrayList<>();
        for (String key : ChangeTypeCatalog.KEYS.get(type)) {
            keysAndValues.add(key);
            keysAndValues.add("value-of-" + key);
        }
        return change(type, keysAndValues.toArray());
    }

    @Test
    void everyTypeOfTheContractHasAPhraseAndNoOtherDoes() {
        assertEquals(ChangeTypeCatalog.KEYS.keySet(), ChangeSentenceService.knownTypes());
    }

    @ParameterizedTest
    @MethodSource("contractTypes")
    void phraseUsesOnlyKeysOfItsType(String type) {
        assertTrue(ChangeTypeCatalog.KEYS.get(type).containsAll(ChangeSentenceService.keysOf(type)), type);
    }

    @ParameterizedTest
    @MethodSource("contractTypes")
    void knownTypeIsPhrasedWithItsOwnSentence(String type) {
        ChangeLine line = sentences.line(withEveryKey(type));
        assertFalse(line.plainText().startsWith("Change "), line.plainText());
        assertFalse(line.plainText().contains("{"), line.plainText());
        assertTrue(line.parts().stream().anyMatch(SentencePart::value), line.plainText());
    }

    @ParameterizedTest
    @MethodSource("contractTypes")
    void onlyTheIdShiftTypesAreMarkedAsShifts(String type) {
        assertEquals(ChangeTypeCatalog.SHIFT_TYPES.contains(type), sentences.line(withEveryKey(type)).shift(), type);
    }

    @Test
    void sentenceShowsTheValuesOfTheChange() {
        ChangeLine line = sentences.line(change("registry.entry_added", "registry", "demo:gadget", "entry", "demo:glossy", "protocol_id", 1));
        assertEquals("Entry demo:glossy was added to registry demo:gadget (id 1).", line.plainText());
        assertEquals(List.of("demo:glossy", "demo:gadget", "1"), line.parts().stream().filter(SentencePart::value).map(SentencePart::text).toList());
    }

    @Test
    void commandPathReadsLikeACommand() {
        ChangeLine line = sentences.line(change("command.node_added", "path", List.of("demo", "spin"), "kind", "literal", "node_count", 3));
        assertEquals("Node demo spin was added (literal, nodes: 3).", line.plainText());
    }

    @Test
    void nullAndObjectValuesAreReadable() {
        ChangeLine line = sentences.line(change("block.default_state_modified", "block", "demo:door",
                "old_default", Map.of("open", "false"), "new_default", Map.of("open", "true")));
        assertEquals("The default state of block demo:door changed from open=false to open=true.", line.plainText());
        ChangeLine nothing = sentences.line(change("registry.default_modified", "registry", "demo:gadget", "old_default", null, "new_default", "demo:glossy"));
        assertEquals("The default entry of registry demo:gadget changed from none to demo:glossy.", nothing.plainText());
    }

    @Test
    void unknownTypeIsRenderedGenericallyWithAllItsFields() {
        ChangeLine line = sentences.line(change("recipe.added", "recipe", "demo:cake", "count", 3));
        assertEquals("Change recipe.added: count 3, recipe demo:cake.", line.plainText());
        assertFalse(line.shift());
        assertTrue(line.parts().stream().filter(SentencePart::value).map(SentencePart::text).toList().contains("recipe.added"));
    }

    @Test
    void unknownTypeWithoutFieldsIsStillShown() {
        assertEquals("Change recipe.cleared.", sentences.line(change("recipe.cleared")).plainText());
    }

    @Test
    void knownTypeMissingAKeyFallsBackToTheGenericRendering() {
        ChangeLine line = sentences.line(change("registry.entry_added", "registry", "demo:gadget", "entry", "demo:glossy"));
        assertEquals("Change registry.entry_added: entry demo:glossy, registry demo:gadget.", line.plainText());
        assertFalse(line.shift());
    }

    @Test
    void shiftFragmentHasNoFinalPeriod() {
        ChangeLine line = sentences.line(change("registry.entry_ids_shifted", "registry", "demo:gadget", "count", 12, "first_id", 3));
        assertTrue(line.shift());
        assertEquals("demo:gadget entries: 12 from id 3", line.plainText());
    }
}
