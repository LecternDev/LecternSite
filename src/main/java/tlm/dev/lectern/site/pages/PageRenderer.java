package tlm.dev.lectern.site.pages;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import java.util.Map;

public final class PageRenderer {

    private static final String VERSION_LIST_TEMPLATE = "versionList.jte";
    private static final String VERSION_PAGE_TEMPLATE = "versionPage.jte";
    private static final String VIEW = "view";

    private final TemplateEngine engine;

    private PageRenderer(TemplateEngine engine) {
        this.engine = engine;
    }

    public static PageRenderer precompiled() {
        return new PageRenderer(TemplateEngine.createPrecompiled(ContentType.Html));
    }

    public String versionList(VersionListView view) {
        return render(VERSION_LIST_TEMPLATE, view);
    }

    public String versionPage(VersionPageView view) {
        return render(VERSION_PAGE_TEMPLATE, view);
    }

    private String render(String template, Object view) {
        StringOutput output = new StringOutput();
        engine.render(template, Map.<String, Object>of(VIEW, view), output);
        return canonical(output.toString());
    }

    private static String canonical(String page) {
        return page.replace("\r\n", "\n").stripTrailing() + "\n";
    }
}
