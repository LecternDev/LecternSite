package tlm.dev.lectern.site.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tlm.dev.lectern.site.fixtures.JsonDoc;
import tlm.dev.lectern.site.json.JsonReader;
import tlm.dev.lectern.site.model.GameInfo;
import tlm.dev.lectern.site.model.PackVersion;
import tlm.dev.lectern.site.model.VersionId;
import tlm.dev.lectern.site.model.VersionKind;
import tlm.dev.lectern.site.model.VersionSheet;

class VersionSheetReaderTest {

    private static final String FILE = "versions/2.0/meta.json";

    private final VersionSheetReader reader = new VersionSheetReader();
    private final JsonReader json = new JsonReader();

    private static JsonDoc valid() {
        return JsonDoc.of(
                "schema_version", "1",
                "id", "\"2.0\"",
                "kind", "\"release\"",
                "release_time", "\"2025-02-10T09:00:00Z\"",
                "step_base", "\"2.0-rc-1\"",
                "release_base", "\"1.5\"");
    }

    private VersionSheet read(JsonDoc doc) {
        return reader.read(FILE, new VersionId("2.0"), json.read(doc.text()));
    }

    private DataFormatException failure(JsonDoc doc) {
        return assertThrows(DataFormatException.class, () -> read(doc));
    }

    @Test
    void everySheetKeyIsRead() {
        VersionSheet sheet = read(valid().with("game", "{\"world_version\": 3001, \"protocol_version\": 777, \"pack_version\": 4}"));
        assertEquals(new VersionId("2.0"), sheet.id());
        assertEquals(new VersionKind.Release(), sheet.kind());
        assertEquals(Instant.parse("2025-02-10T09:00:00Z"), sheet.releaseTime());
        assertEquals(Optional.of(new VersionId("2.0-rc-1")), sheet.stepBase());
        assertEquals(Optional.of(new VersionId("1.5")), sheet.releaseBase());
        assertEquals(
                Optional.of(new GameInfo(OptionalLong.of(3001), OptionalLong.of(777), Optional.of(new PackVersion.Single(4)))),
                sheet.game());
    }

    @Test
    void nullBasesAreAbsentBases() {
        VersionSheet sheet = read(valid().with("step_base", "null").with("release_base", "null"));
        assertEquals(Optional.empty(), sheet.stepBase());
        assertEquals(Optional.empty(), sheet.releaseBase());
    }

    @Test
    void packVersionAcceptsAnInteger() {
        assertEquals(Optional.of(new PackVersion.Single(4)), pack("4"));
    }

    @Test
    void packVersionAcceptsDataAndResource() {
        assertEquals(Optional.of(new PackVersion.Split(26, 22)), pack("{\"data\": 26, \"resource\": 22}"));
    }

    @Test
    void packVersionAcceptsMajorAndMinorParts() {
        assertEquals(
                Optional.of(new PackVersion.Versioned(121, 0, 97, 1)),
                pack("{\"data_major\": 121, \"data_minor\": 0, \"resource_major\": 97, \"resource_minor\": 1}"));
    }

    private Optional<PackVersion> pack(String rawPack) {
        return read(valid().with("game", "{\"pack_version\": " + rawPack + "}")).game().orElseThrow().packVersion();
    }

    @Test
    void gameKeysTheJarDoesNotHaveAreLeftOut() {
        GameInfo game = read(valid().with("game", "{\"world_version\": 3001}")).game().orElseThrow();
        assertEquals(OptionalLong.of(3001), game.worldVersion());
        assertEquals(OptionalLong.empty(), game.protocolVersion());
        assertEquals(Optional.empty(), game.packVersion());
    }

    @Test
    void sheetWithoutGameIsRead() {
        assertEquals(Optional.empty(), read(valid()).game());
    }

    @Test
    void extraKeysAreIgnored() {
        VersionSheet sheet = read(valid()
                .with("jar_sha1", "\"0000000000000000000000000000000000000004\"")
                .with("future_key", "{\"anything\": [1, 2]}")
                .with("game", "{\"world_version\": 5, \"future_game_key\": true, \"build_time\": \"x\", \"stable\": false}"));
        assertEquals(OptionalLong.of(5), sheet.game().orElseThrow().worldVersion());
    }

    @Test
    void unknownKindStopsAndNamesTheFile() {
        DataFormatException failure = failure(valid().with("kind", "\"beta\""));
        assertTrue(failure.getMessage().startsWith(FILE + ":"), failure.getMessage());
        assertTrue(failure.getMessage().contains("'beta'"), failure.getMessage());
    }

    @Test
    void newerSchemaVersionStopsAndNamesTheFile() {
        DataFormatException failure = failure(valid().with("schema_version", "2"));
        assertTrue(failure.getMessage().startsWith(FILE + ":"), failure.getMessage());
        assertTrue(failure.getMessage().contains("newer"), failure.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "\"1\"", "1.5", "null"})
    void unusableSchemaVersionStops(String raw) {
        assertThrows(DataFormatException.class, () -> read(valid().with("schema_version", raw)));
    }

    @Test
    void missingSchemaVersionStops() {
        assertTrue(failure(valid().without("schema_version")).getMessage().contains("'schema_version'"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "kind", "release_time", "step_base", "release_base"})
    void missingRequiredKeyStopsAndNamesTheKey(String key) {
        DataFormatException failure = failure(valid().without(key));
        assertTrue(failure.getMessage().startsWith(FILE + ":"), failure.getMessage());
        assertTrue(failure.getMessage().contains("'" + key + "'"), failure.getMessage());
    }

    @Test
    void idThatIsNotTheFolderStops() {
        assertTrue(failure(valid().with("id", "\"2.1\"")).getMessage().contains("folder"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"yesterday\"", "20250210", "null", "\"2025-02-10\""})
    void releaseTimeThatIsNotAnInstantStops(String raw) {
        assertThrows(DataFormatException.class, () -> read(valid().with("release_time", raw)));
    }

    @Test
    void baseThatIsNotAVersionIdStops() {
        assertTrue(failure(valid().with("step_base", "\"a/b\"")).getMessage().contains("'step_base'"));
    }

    @Test
    void packVersionWithAnUnknownShapeStops() {
        DataFormatException failure = assertThrows(DataFormatException.class, () -> pack("{\"data\": 1}"));
        assertTrue(failure.getMessage().contains("pack_version"), failure.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"four\"", "-4", "4.5", "[4]", "true"})
    void packVersionOfAnotherTypeStops(String raw) {
        assertThrows(DataFormatException.class, () -> pack(raw));
    }

    @Test
    void worldVersionThatIsNotAnIntegerStops() {
        DataFormatException failure = assertThrows(
                DataFormatException.class, () -> read(valid().with("game", "{\"world_version\": \"5\"}")));
        assertTrue(failure.getMessage().contains("'game.world_version'"), failure.getMessage());
    }

    @Test
    void gameThatIsNotAnObjectStops() {
        assertThrows(DataFormatException.class, () -> read(valid().with("game", "[1]")));
    }

    @Test
    void documentThatIsNotAnObjectStops() {
        assertThrows(DataFormatException.class, () -> reader.read(FILE, new VersionId("2.0"), json.read("[]")));
    }
}
