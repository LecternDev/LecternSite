package tlm.dev.lectern.site.pages;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tlm.dev.lectern.site.fixtures.VersionFixtures;

class PageWriterTest {

    private static final Path SITE_OUTPUT = Path.of("build", "site");

    private final PageRenderer renderer = PageRenderer.precompiled();

    @Test
    void versionListIsWrittenUnderBuildSite() throws IOException {
        Path written = new PageWriter(renderer, SITE_OUTPUT).writeVersionList(VersionFixtures.threeVersions());

        assertEquals(SITE_OUTPUT.resolve("versions").resolve("index.html"), written);
        String page = Files.readString(written, StandardCharsets.UTF_8);
        assertTrue(HtmlRows.rowOf(page, "25w14craftmine").contains(HtmlRows.UNSTABLE_LABEL));
        assertTrue(HtmlRows.rowOf(page, "1.21.5-rc1").contains(HtmlRows.UNSTABLE_LABEL));
        assertFalse(HtmlRows.rowOf(page, "1.21.5").contains("unstable"));
    }

    @Test
    void writtenFileIsTheRenderedPageInUtf8WithoutBom(@TempDir Path root) throws IOException {
        Path written = new PageWriter(renderer, root).writeVersionList(VersionFixtures.threeVersions());

        byte[] bytes = Files.readAllBytes(written);
        assertArrayEquals(renderer.versionList(VersionFixtures.threeVersions()).getBytes(StandardCharsets.UTF_8), bytes);
        assertEquals('<', (char) bytes[0]);
    }

    @Test
    void writingTwiceGivesTheSameBytes(@TempDir Path root) throws IOException {
        PageWriter writer = new PageWriter(renderer, root);
        byte[] first = Files.readAllBytes(writer.writeVersionList(VersionFixtures.threeVersions()));
        byte[] second = Files.readAllBytes(writer.writeVersionList(VersionFixtures.threeVersions()));
        assertArrayEquals(first, second);
    }

    @Test
    void failureNamesTheTargetFile(@TempDir Path root) throws IOException {
        Path blocker = Files.writeString(root.resolve("site"), "not a directory");
        PageWriter writer = new PageWriter(renderer, blocker);

        PageWriteException failure = assertThrows(PageWriteException.class, () -> writer.writeVersionList(VersionFixtures.threeVersions()));

        assertTrue(failure.getMessage().contains(blocker.resolve("versions").resolve("index.html").toString()));
    }
}
