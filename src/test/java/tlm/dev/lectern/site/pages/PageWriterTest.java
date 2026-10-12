package tlm.dev.lectern.site.pages;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tlm.dev.lectern.site.fixtures.VersionFixtures;
import tlm.dev.lectern.site.model.SiteData;

class PageWriterTest {

    private static PageWriter writer(Path root) {
        PageFrames frames = new PageFrames(Optional.empty());
        VersionPageService versionPages =
                new VersionPageService(frames, new ChangeSectionService(new ChangeSentenceService(new ChangeValueRenderer())));
        return new PageWriter(PageRenderer.precompiled(), frames, versionPages, root);
    }

    private static List<String> relativeFiles(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile).map(file -> root.relativize(file).toString().replace('\\', '/')).sorted().toList();
        }
    }

    @Test
    void oneHomePageAndOnePagePerVersionAreWritten(@TempDir Path root) throws IOException {
        SiteData data = VersionFixtures.threeVersionSite();
        List<Path> written = writer(root).writeSite(data);

        assertEquals(4, written.size());
        assertEquals(
                List.of("index.html", "versions/1.21.5-rc1/index.html", "versions/1.21.5/index.html", "versions/25w14craftmine/index.html"),
                relativeFiles(root));
    }

    @Test
    void homePageIsWrittenFirstAndThenTheVersionsNewestFirst(@TempDir Path root) {
        List<Path> written = writer(root).writeSite(VersionFixtures.threeVersionSite());
        assertEquals(
                List.of(
                        root.resolve("index.html"),
                        root.resolve("versions").resolve("25w14craftmine").resolve("index.html"),
                        root.resolve("versions").resolve("1.21.5").resolve("index.html"),
                        root.resolve("versions").resolve("1.21.5-rc1").resolve("index.html")),
                written);
    }

    @Test
    void emptySiteStillGetsAHomePage(@TempDir Path root) throws IOException {
        writer(root).writeSite(new SiteData(List.of()));
        assertEquals(List.of("index.html"), relativeFiles(root));
    }

    @Test
    void filesAreUtf8WithoutBomAndEqualToTheRenderedPage(@TempDir Path root) throws IOException {
        writer(root).writeSite(VersionFixtures.threeVersionSite());
        byte[] bytes = Files.readAllBytes(root.resolve("index.html"));
        assertEquals('<', (char) bytes[0]);
        assertTrue(new String(bytes, StandardCharsets.UTF_8).contains("<h1>Versions</h1>"));
    }

    @Test
    void writingTwiceGivesTheSameBytesForEveryFile(@TempDir Path base) throws IOException {
        Path first = base.resolve("first");
        Path second = base.resolve("second");
        writer(first).writeSite(VersionFixtures.threeVersionSite());
        writer(second).writeSite(VersionFixtures.threeVersionSite());
        writer(first).writeSite(VersionFixtures.threeVersionSite());

        assertEquals(relativeFiles(first), relativeFiles(second));
        for (String file : relativeFiles(first)) {
            assertArrayEquals(Files.readAllBytes(first.resolve(file)), Files.readAllBytes(second.resolve(file)), file);
        }
    }

    @Test
    void failureNamesTheTargetFile(@TempDir Path root) throws IOException {
        Path blocker = Files.writeString(root.resolve("site"), "not a directory");

        PageWriteException failure = assertThrows(
                PageWriteException.class, () -> writer(blocker).writeSite(VersionFixtures.threeVersionSite()));

        assertTrue(failure.getMessage().contains(blocker.resolve("index.html").toString()), failure.getMessage());
    }
}
