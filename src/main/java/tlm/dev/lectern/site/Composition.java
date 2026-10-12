package tlm.dev.lectern.site;

import tlm.dev.lectern.site.data.ChangeSetReader;
import tlm.dev.lectern.site.data.LecternDataReader;
import tlm.dev.lectern.site.data.VersionSheetReader;
import tlm.dev.lectern.site.json.JsonReader;
import tlm.dev.lectern.site.pages.ChangeSectionService;
import tlm.dev.lectern.site.pages.ChangeSentenceService;
import tlm.dev.lectern.site.pages.ChangeValueRenderer;
import tlm.dev.lectern.site.pages.PageFrames;
import tlm.dev.lectern.site.pages.PageRenderer;
import tlm.dev.lectern.site.pages.PageWriter;
import tlm.dev.lectern.site.pages.VersionPageService;

final class Composition {

    private Composition() {
    }

    static SiteGenerator generator(CommandLine command) {
        LecternDataReader reader =
                new LecternDataReader(command.data(), new JsonReader(), new VersionSheetReader(), new ChangeSetReader());
        PageFrames frames = new PageFrames(command.contact());
        ChangeSectionService sections = new ChangeSectionService(new ChangeSentenceService(new ChangeValueRenderer()));
        PageWriter writer = new PageWriter(PageRenderer.precompiled(), frames, new VersionPageService(frames, sections), command.out());
        return new SiteGenerator(reader, writer);
    }
}
