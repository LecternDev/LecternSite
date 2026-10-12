package tlm.dev.lectern.site.pages;

import java.io.IOException;
import java.nio.file.Path;

public final class PageWriteException extends RuntimeException {

    public PageWriteException(Path target, IOException cause) {
        super("Cannot write page " + target, cause);
    }
}
