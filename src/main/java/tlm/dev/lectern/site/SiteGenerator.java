package tlm.dev.lectern.site;

import tlm.dev.lectern.site.data.LecternDataReader;
import tlm.dev.lectern.site.pages.PageWriter;

final class SiteGenerator {

    private final LecternDataReader reader;
    private final PageWriter writer;

    SiteGenerator(LecternDataReader reader, PageWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    int generate() {
        return writer.writeSite(reader.read()).size();
    }
}
