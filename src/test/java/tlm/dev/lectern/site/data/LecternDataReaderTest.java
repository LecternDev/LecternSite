package tlm.dev.lectern.site.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tlm.dev.lectern.site.fixtures.JsonDoc;
import tlm.dev.lectern.site.json.JsonReader;
import tlm.dev.lectern.site.model.SiteData;
import tlm.dev.lectern.site.model.VersionData;

class LecternDataReaderTest {

    private static final Path INVENTED = Path.of("src/test/resources/lectern-data");

    private static SiteData readInvented() {
        return reader(INVENTED).read();
    }

    private static LecternDataReader reader(Path root) {
        return new LecternDataReader(root, new JsonReader(), new VersionSheetReader(), new ChangeSetReader());
    }

    private static Path copyOfInvented(Path target) throws IOException {
        try (Stream<Path> files = Files.walk(INVENTED)) {
            for (Path source : files.toList()) {
                Path destination = target.resolve(INVENTED.relativize(source).toString());
                if (Files.isDirectory(source)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(source, destination);
                }
            }
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        return target;
    }

    private static JsonDoc diff(String from, String fromKind, String to, String toKind) {
        return JsonDoc.of(
                "schema_version", "1",
                "from", "{\"id\": \"" + from + "\", \"kind\": \"" + fromKind + "\"}",
                "to", "{\"id\": \"" + to + "\", \"kind\": \"" + toKind + "\"}",
                "skipped", "[]",
                "changes", "[]");
    }

    private static JsonDoc sheet(String id) {
        return JsonDoc.of(
                "schema_version", "1",
                "id", "\"" + id + "\"",
                "kind", "\"release\"",
                "release_time", "\"2025-02-10T09:00:00Z\"",
                "step_base", "null",
                "release_base", "null");
    }

    private static VersionData version(SiteData data, String id) {
        return data.versions().stream()
                .filter(version -> version.sheet().id().value().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private static DataFormatException failure(Path root) {
        return assertThrows(DataFormatException.class, () -> reader(root).read());
    }

    @Test
    void versionsAreReadNewestFirst() {
        assertEquals(
                List.of("2.1-snapshot-1", "2.0", "2.0-rc-1", "1.5", "1.0"),
                readInvented().versions().stream().map(version -> version.sheet().id().value()).toList());
    }

    @Test
    void stepAndReleaseDiffsAreAttachedToTheirVersion() {
        SiteData data = readInvented();
        assertEquals(0, version(data, "2.0").stepChanges().orElseThrow().changes().size());
        assertEquals(7, version(data, "2.0").releaseChanges().orElseThrow().changes().size());
        assertEquals(9, version(data, "2.0-rc-1").stepChanges().orElseThrow().changes().size());
        assertTrue(version(data, "2.0-rc-1").releaseChanges().isEmpty());
        assertEquals(5, version(data, "2.1-snapshot-1").stepChanges().orElseThrow().changes().size());
    }

    @Test
    void versionsWithoutABaseHaveNoDiff() {
        SiteData data = readInvented();
        assertTrue(version(data, "1.0").stepChanges().isEmpty());
        assertTrue(version(data, "1.5").releaseChanges().isEmpty());
    }

    @Test
    void emptyVersionsFolderGivesAnEmptySite(@TempDir Path root) throws IOException {
        Files.createDirectories(root.resolve("versions"));
        assertEquals(List.of(), reader(root).read().versions());
    }

    @Test
    void repositoryWithoutDiffsFolderIsReadWhenNoVersionHasABase(@TempDir Path root) throws IOException {
        Files.createDirectories(root.resolve("versions/1.0"));
        Files.writeString(root.resolve("versions/1.0/meta.json"), sheet("1.0").text());
        assertEquals(1, reader(root).read().versions().size());
    }

    @Test
    void missingDiffsFolderStopsWhenASheetNamesABase(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        try (Stream<Path> files = Files.walk(copy.resolve("diffs"))) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(file);
            }
        }
        DataFormatException failure = failure(copy);
        assertTrue(failure.getMessage().contains("diffs/"), failure.getMessage());
        assertTrue(failure.getMessage().contains("step_base"), failure.getMessage());
    }

    @Test
    void folderThatIsNotALecternDataCloneStopsAndNamesIt(@TempDir Path root) {
        DataFormatException failure = failure(root);
        assertTrue(failure.getMessage().contains(root.toString()), failure.getMessage());
        assertTrue(failure.getMessage().contains("versions"), failure.getMessage());
    }

    @Test
    void entryThatIsNotAFolderStops(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.writeString(copy.resolve("versions/notes.txt"), "stray");
        assertTrue(failure(copy).getMessage().startsWith("versions/notes.txt:"));
    }

    @Test
    void versionFolderWithoutASheetStops(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.createDirectories(copy.resolve("versions/3.0"));
        assertTrue(failure(copy).getMessage().startsWith("versions/3.0/meta.json:"));
    }

    @Test
    void sheetThatIsNotJsonStopsAndNamesTheFile(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.writeString(copy.resolve("versions/1.0/meta.json"), "{\"id\": ");
        assertTrue(failure(copy).getMessage().startsWith("versions/1.0/meta.json:"));
    }

    @Test
    void sheetThatIsNotUtf8StopsAndNamesTheFile(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.write(copy.resolve("versions/1.0/meta.json"), new byte[] {'{', (byte) 0xC3, (byte) 0x28, '}'});
        assertTrue(failure(copy).getMessage().startsWith("versions/1.0/meta.json:"));
    }

    @Test
    void sheetWithAnotherIdThanItsFolderStops(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.writeString(copy.resolve("versions/1.0/meta.json"), sheet("1.1").text());
        assertTrue(failure(copy).getMessage().startsWith("versions/1.0/meta.json:"));
    }

    @Test
    void diffFolderWithoutASheetStops(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.createDirectories(copy.resolve("diffs/9.9"));
        assertTrue(failure(copy).getMessage().startsWith("diffs/9.9:"));
    }

    @Test
    void missingDiffForANamedBaseStops(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.delete(copy.resolve("diffs/2.1-snapshot-1/step.json"));
        DataFormatException failure = failure(copy);
        assertTrue(failure.getMessage().startsWith("diffs/2.1-snapshot-1/step.json:"), failure.getMessage());
        assertTrue(failure.getMessage().contains("step_base"), failure.getMessage());
    }

    @Test
    void diffForAVersionWithoutABaseStops(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.copy(copy.resolve("diffs/2.0-rc-1/step.json"), copy.resolve("diffs/2.0-rc-1/release.json"));
        DataFormatException failure = failure(copy);
        assertTrue(failure.getMessage().startsWith("diffs/2.0-rc-1/release.json:"), failure.getMessage());
        assertTrue(failure.getMessage().contains("release_base"), failure.getMessage());
    }

    @Test
    void diffThatEndsAtAnotherVersionStops(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.writeString(copy.resolve("diffs/2.0/step.json"), diff("2.0-rc-1", "release-candidate", "2.9", "release").text());
        assertTrue(failure(copy).getMessage().startsWith("diffs/2.0/step.json:"));
    }

    @Test
    void diffWhoseKindDisagreesWithTheSheetStops(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.writeString(copy.resolve("diffs/2.0/step.json"), diff("2.0-rc-1", "release-candidate", "2.0", "snapshot").text());
        DataFormatException failure = failure(copy);
        assertTrue(failure.getMessage().startsWith("diffs/2.0/step.json:"), failure.getMessage());
        assertTrue(failure.getMessage().contains("kind"), failure.getMessage());
    }

    @Test
    void diffThatStartsAtAnotherBaseThanTheSheetStops(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.writeString(copy.resolve("diffs/2.0/step.json"), diff("1.5", "release", "2.0", "release").text());
        assertTrue(failure(copy).getMessage().startsWith("diffs/2.0/step.json:"));
    }

    @Test
    void diffWithANewerSchemaStopsAndNamesTheFile(@TempDir Path root) throws IOException {
        Path copy = copyOfInvented(root);
        Files.writeString(
                copy.resolve("diffs/2.1-snapshot-1/step.json"),
                diff("2.0", "release", "2.1-snapshot-1", "snapshot").with("schema_version", "2").text());
        DataFormatException failure = failure(copy);
        assertTrue(failure.getMessage().startsWith("diffs/2.1-snapshot-1/step.json:"), failure.getMessage());
        assertTrue(failure.getMessage().contains("newer"), failure.getMessage());
    }
}
