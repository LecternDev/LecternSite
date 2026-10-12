package tlm.dev.lectern.site.pages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import tlm.dev.lectern.site.model.SiteData;
import tlm.dev.lectern.site.model.VersionData;
import tlm.dev.lectern.site.model.VersionId;

public final class PageWriter {

    private static final String LIST_TITLE = "Versions";

    private final PageRenderer renderer;
    private final PageFrames frames;
    private final VersionPageService versionPages;
    private final Path root;

    public PageWriter(PageRenderer renderer, PageFrames frames, VersionPageService versionPages, Path root) {
        this.renderer = renderer;
        this.frames = frames;
        this.versionPages = versionPages;
        this.root = root;
    }

    public List<Path> writeSite(SiteData data) {
        List<Path> written = new ArrayList<>();
        PageFrame listFrame = frames.frame(LIST_TITLE, SiteUrls.HOME_DEPTH);
        written.add(write(SiteUrls.HOME_FILE, renderer.versionList(VersionListView.of(listFrame, data))));
        Set<VersionId> published = data.ids();
        for (VersionData version : data.versions()) {
            String page = renderer.versionPage(versionPages.view(version, published));
            written.add(write(SiteUrls.versionFile(version.sheet().id()), page));
        }
        return written;
    }

    private Path write(Path relative, String page) {
        Path target = root.resolve(relative);
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, page, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PageWriteException(target, e);
        }
        return target;
    }
}
