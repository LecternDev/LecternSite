package tlm.dev.lectern.site;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SourceRulesTest {

    private static final Path MAIN = Path.of("src/main/java");
    private static final Path TEST = Path.of("src/test/java");
    private static final Path SITE = MAIN.resolve("tlm/dev/lectern/site");

    @Test
    void mainSourcesHaveNoComments() throws IOException {
        assertEquals(List.of(), commentOffenders(MAIN));
    }

    @Test
    void testSourcesHaveNoComments() throws IOException {
        assertEquals(List.of(), commentOffenders(TEST));
    }

    @Test
    void mainSourcesAreAsciiOnly() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path file : javaFiles(MAIN)) {
            for (int line : SourceScanner.nonAsciiLines(read(file))) {
                offenders.add(file + ":" + line);
            }
        }
        assertEquals(List.of(), offenders);
    }

    @Test
    void familySeamsStayEmpty() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path file : javaFiles(SITE)) {
            String relative = SITE.relativize(file).toString().replace('\\', '/');
            offenders.addAll(SeamRules.violations(relative, read(file)));
        }
        assertEquals(List.of(), offenders);
    }

    private static List<String> commentOffenders(Path directory) throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path file : javaFiles(directory)) {
            for (int line : SourceScanner.scan(read(file)).commentLines()) {
                offenders.add(file + ":" + line);
            }
        }
        return offenders;
    }

    private static List<Path> javaFiles(Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
    }

    private static String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
