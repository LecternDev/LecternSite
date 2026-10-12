package tlm.dev.lectern.site;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    private static final String INVENTED = "src/test/resources/lectern-data";

    private record Outcome(int exitCode, String out, String err) {
    }

    private static Outcome run(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitCode = Main.run(args, new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
        return new Outcome(exitCode, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    private static List<String> relativeFiles(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile).map(file -> root.relativize(file).toString().replace('\\', '/')).sorted().toList();
        }
    }

    @Test
    void generationWritesTheHomePageAndOnePagePerVersionAtTheirPermalinks(@TempDir Path out) throws IOException {
        Outcome outcome = run("--data", INVENTED, "--out", out.toString());

        assertEquals(Main.EXIT_OK, outcome.exitCode());
        assertEquals("Wrote 6 pages to " + out + "\n", outcome.out());
        assertEquals("", outcome.err());
        assertEquals(
                List.of(
                        "index.html",
                        "versions/1.0/index.html",
                        "versions/1.5/index.html",
                        "versions/2.0-rc-1/index.html",
                        "versions/2.0/index.html",
                        "versions/2.1-snapshot-1/index.html"),
                relativeFiles(out));
    }

    @Test
    void contactGivenOnTheCommandLineIsOnEveryPage(@TempDir Path out) throws IOException {
        run("--data", INVENTED, "--out", out.toString(), "--contact", "team@example.org");
        for (String file : relativeFiles(out)) {
            String html = Files.readString(out.resolve(file), StandardCharsets.UTF_8);
            assertTrue(html.contains("mailto:team@example.org"), file);
            assertTrue(html.contains("NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT."), file);
        }
    }

    @Test
    void publicBuildWithAContactGenerates(@TempDir Path out) {
        Outcome outcome = run("--data", INVENTED, "--out", out.toString(), "--contact", "team@example.org", "--public");
        assertEquals(Main.EXIT_OK, outcome.exitCode());
    }

    @Test
    void publicBuildWithoutAContactStopsAndWritesNothing(@TempDir Path base) {
        Path out = base.resolve("site");
        Outcome outcome = run("--data", INVENTED, "--out", out.toString(), "--public");

        assertEquals(Main.EXIT_USAGE, outcome.exitCode());
        assertEquals("", outcome.out());
        assertTrue(outcome.err().startsWith("error: option --public needs --contact"), outcome.err());
        assertTrue(outcome.err().contains(CommandLine.USAGE));
        assertFalse(Files.exists(out));
    }

    @Test
    void missingDataIsAUsageErrorWithExitCodeTwo() {
        Outcome outcome = run("--out", "site");
        assertEquals(Main.EXIT_USAGE, outcome.exitCode());
        assertTrue(outcome.err().contains("missing option --data"));
    }

    @Test
    void folderThatIsNotALecternDataCloneIsAFailureNamingIt(@TempDir Path base) {
        Path notAClone = base.resolve("empty");
        Outcome outcome = run("--data", notAClone.toString(), "--out", base.resolve("site").toString());

        assertEquals(Main.EXIT_FAILURE, outcome.exitCode());
        assertEquals("", outcome.out());
        assertTrue(outcome.err().startsWith("error: " + notAClone), outcome.err());
        assertFalse(Files.exists(base.resolve("site")));
    }

    @Test
    void unreadableSheetIsAFailureNamingTheFileAndWritesNothing(@TempDir Path base) throws IOException {
        Path data = base.resolve("data");
        Files.createDirectories(data.resolve("versions/1.0"));
        Files.writeString(data.resolve("versions/1.0/meta.json"), "{\"schema_version\": 9}");
        Path out = base.resolve("site");

        Outcome outcome = run("--data", data.toString(), "--out", out.toString());

        assertEquals(Main.EXIT_FAILURE, outcome.exitCode());
        assertTrue(outcome.err().startsWith("error: versions/1.0/meta.json:"), outcome.err());
        assertFalse(Files.exists(out));
    }

    @Test
    void twoGenerationsGiveTheSameBytes(@TempDir Path base) throws IOException {
        Path first = base.resolve("first");
        Path second = base.resolve("second");
        run("--data", INVENTED, "--out", first.toString(), "--contact", "team@example.org");
        run("--data", INVENTED, "--out", second.toString(), "--contact", "team@example.org");

        assertEquals(relativeFiles(first), relativeFiles(second));
        for (String file : relativeFiles(first)) {
            assertArrayEquals(Files.readAllBytes(first.resolve(file)), Files.readAllBytes(second.resolve(file)), file);
        }
    }

    @Test
    void generatingAgainOverTheSameFolderKeepsTheSameBytes(@TempDir Path out) throws IOException {
        run("--data", INVENTED, "--out", out.toString());
        byte[] before = Files.readAllBytes(out.resolve("versions/2.0/index.html"));
        run("--data", INVENTED, "--out", out.toString());
        assertArrayEquals(before, Files.readAllBytes(out.resolve("versions/2.0/index.html")));
    }
}
