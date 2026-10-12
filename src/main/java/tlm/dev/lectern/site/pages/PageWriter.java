package tlm.dev.lectern.site.pages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import tlm.dev.lectern.site.model.VersionSummary;

public final class PageWriter {

    static final Path VERSION_LIST_PAGE = Path.of("versions", "index.html");

    private final PageRenderer renderer;
    private final Path root;

    public PageWriter(PageRenderer renderer, Path root) {
        this.renderer = renderer;
        this.root = root;
    }

    public Path writeVersionList(List<VersionSummary> versions) {
        return write(VERSION_LIST_PAGE, renderer.versionList(versions));
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
