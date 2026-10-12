package tlm.dev.lectern.site.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tlm.dev.lectern.site.fixtures.JsonDoc;
import tlm.dev.lectern.site.json.JsonReader;
import tlm.dev.lectern.site.model.Change;
import tlm.dev.lectern.site.model.ChangeSet;
import tlm.dev.lectern.site.model.ChangeType;
import tlm.dev.lectern.site.model.ChangeValue;
import tlm.dev.lectern.site.model.SkippedDomain;
import tlm.dev.lectern.site.model.SkippedSide;
import tlm.dev.lectern.site.model.VersionId;
import tlm.dev.lectern.site.model.VersionKind;
import tlm.dev.lectern.site.model.VersionRef;

class ChangeSetReaderTest {

    private static final String FILE = "diffs/2.0/step.json";
    private static final String ADDED = "{\"type\": \"registry.entry_added\", \"registry\": \"demo:gadget\", \"entry\": \"demo:glossy\", \"protocol_id\": 1}";

    private final ChangeSetReader reader = new ChangeSetReader();
    private final JsonReader json = new JsonReader();

    private static JsonDoc valid() {
        return JsonDoc.of(
                "schema_version", "1",
                "from", "{\"id\": \"2.0-rc-1\", \"kind\": \"release-candidate\"}",
                "to", "{\"id\": \"2.0\", \"kind\": \"release\"}",
                "skipped", "[]",
                "changes", "[" + ADDED + "]");
    }

    private ChangeSet read(JsonDoc doc) {
        return reader.read(FILE, json.read(doc.text()));
    }

    private DataFormatException failure(JsonDoc doc) {
        return assertThrows(DataFormatException.class, () -> read(doc));
    }

    @Test
    void envelopeIsRead() {
        ChangeSet changes = read(valid());
        assertEquals(new VersionRef(new VersionId("2.0-rc-1"), new VersionKind.ReleaseCandidate()), changes.from());
        assertEquals(new VersionRef(new VersionId("2.0"), new VersionKind.Release()), changes.to());
        assertEquals(List.of(), changes.skipped());
        assertEquals(1, changes.changes().size());
    }

    @Test
    void changeKeepsItsTypeSubjectAndPayload() {
        Change change = read(valid()).changes().getFirst();
        assertEquals(new ChangeType("registry.entry_added"), change.type());
        assertEquals(
                Map.of("entry", new ChangeValue.Text("demo:glossy"), "registry", new ChangeValue.Text("demo:gadget")),
                change.subject());
        assertEquals(Map.of("protocol_id", new ChangeValue.Numeric("1")), change.payload());
    }

    @Test
    void everyKindOfPayloadValueIsRead() {
        String raw = "{\"type\": \"demo.all\", \"a\": \"text\", \"b\": 0.0, \"c\": true, \"d\": null, \"e\": [\"x\", 2], \"f\": {\"g\": \"h\"}}";
        Change change = read(valid().with("changes", "[" + raw + "]")).changes().getFirst();
        assertEquals(new ChangeValue.Text("text"), change.value("a").orElseThrow());
        assertEquals(new ChangeValue.Numeric("0.0"), change.value("b").orElseThrow());
        assertEquals(new ChangeValue.Flag(true), change.value("c").orElseThrow());
        assertEquals(new ChangeValue.Nothing(), change.value("d").orElseThrow());
        assertEquals(new ChangeValue.Items(List.of(new ChangeValue.Text("x"), new ChangeValue.Numeric("2"))), change.value("e").orElseThrow());
        assertEquals(new ChangeValue.Fields(Map.of("g", new ChangeValue.Text("h"))), change.value("f").orElseThrow());
        assertEquals(Optional.empty(), change.value("missing"));
    }

    @Test
    void typeTheSiteDoesNotKnowIsKept() {
        String raw = "{\"type\": \"recipe.added\", \"recipe\": \"demo:cake\", \"count\": 3}";
        Change change = read(valid().with("changes", "[" + raw + "]")).changes().getFirst();
        assertEquals("recipe.added", change.type().name());
        assertEquals(new ChangeValue.Text("demo:cake"), change.value("recipe").orElseThrow());
    }

    @Test
    void skippedDomainsAreRead() {
        String raw = "[{\"domain\": \"packets\", \"reason\": \"report_missing\", \"side\": \"both\"}]";
        assertEquals(
                List.of(new SkippedDomain("packets", new SkippedSide.Both(), "report_missing")),
                read(valid().with("skipped", raw)).skipped());
    }

    @Test
    void extraKeysAreIgnored() {
        ChangeSet changes = read(valid()
                .with("future_key", "[1]")
                .with("from", "{\"id\": \"2.0-rc-1\", \"kind\": \"release-candidate\", \"extra\": 1}"));
        assertEquals(1, changes.changes().size());
    }

    @Test
    void emptyChangesAreRead() {
        assertEquals(List.of(), read(valid().with("changes", "[]")).changes());
    }

    @Test
    void newerSchemaVersionStopsAndNamesTheFile() {
        DataFormatException failure = failure(valid().with("schema_version", "2"));
        assertTrue(failure.getMessage().startsWith(FILE + ":"), failure.getMessage());
        assertTrue(failure.getMessage().contains("newer"), failure.getMessage());
    }

    @Test
    void unknownKindStopsAndNamesTheFile() {
        DataFormatException failure = failure(valid().with("to", "{\"id\": \"2.0\", \"kind\": \"beta\"}"));
        assertTrue(failure.getMessage().startsWith(FILE + ":"), failure.getMessage());
        assertTrue(failure.getMessage().contains("'beta'"), failure.getMessage());
    }

    @Test
    void unknownSkippedSideStops() {
        String raw = "[{\"domain\": \"packets\", \"reason\": \"report_missing\", \"side\": \"middle\"}]";
        assertTrue(failure(valid().with("skipped", raw)).getMessage().contains("'skipped[0].side'"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"from", "to", "skipped", "changes", "schema_version"})
    void missingEnvelopeKeyStopsAndNamesTheKey(String key) {
        DataFormatException failure = failure(valid().without(key));
        assertTrue(failure.getMessage().startsWith(FILE + ":"), failure.getMessage());
        assertTrue(failure.getMessage().contains("'" + key + "'"), failure.getMessage());
    }

    @Test
    void changeWithoutATypeStopsAndNamesItsPosition() {
        String raw = ADDED + ", {\"registry\": \"demo:gadget\"}";
        assertTrue(failure(valid().with("changes", "[" + raw + "]")).getMessage().contains("'changes[1].type'"));
    }

    @Test
    void changeThatIsNotAnObjectStops() {
        assertTrue(failure(valid().with("changes", "[1]")).getMessage().contains("changes"));
    }

    @Test
    void changeTypeWithWhitespaceStops() {
        String raw = "{\"type\": \"registry added\"}";
        assertTrue(failure(valid().with("changes", "[" + raw + "]")).getMessage().contains("'changes[0].type'"));
    }

    @Test
    void versionIdWithASeparatorStops() {
        DataFormatException failure = failure(valid().with("from", "{\"id\": \"a/b\", \"kind\": \"release\"}"));
        assertTrue(failure.getMessage().contains("'from.id'"), failure.getMessage());
    }

    @Test
    void documentThatIsNotAnObjectStops() {
        assertThrows(DataFormatException.class, () -> reader.read(FILE, json.read("[]")));
    }
}
