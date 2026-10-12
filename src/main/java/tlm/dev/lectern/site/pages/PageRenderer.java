package tlm.dev.lectern.site.pages;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import java.util.List;
import java.util.Map;
import tlm.dev.lectern.site.model.VersionSummary;

public final class PageRenderer {

    private static final String VERSION_LIST_TEMPLATE = "versions.jte";

    private final TemplateEngine engine;

    private PageRenderer(TemplateEngine engine) {
        this.engine = engine;
    }

    public static PageRenderer precompiled() {
        return new PageRenderer(TemplateEngine.createPrecompiled(ContentType.Html));
    }

    public String versionList(List<VersionSummary> versions) {
        List<VersionRow> rows = versions.stream()
                .sorted(VersionSummary.NEWEST_FIRST)
                .map(VersionRow::from)
                .toList();
        StringOutput output = new StringOutput();
        engine.render(VERSION_LIST_TEMPLATE, Map.<String, Object>of("rows", rows), output);
        return canonical(output.toString());
    }

    private static String canonical(String page) {
        return page.replace("\r\n", "\n").stripTrailing() + "\n";
    }
}
