package tlm.dev.lectern.site.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import tlm.dev.lectern.site.model.VersionId;

class SiteUrlsTest {

    @Test
    void homePageIsTheIndexAtTheRoot() {
        assertEquals(Path.of("index.html"), SiteUrls.HOME_FILE);
        assertEquals("", SiteUrls.root(SiteUrls.HOME_DEPTH));
    }

    @Test
    void versionPageIsAnIndexInAFolderNamedLikeTheVersion() {
        assertEquals(Path.of("versions", "26.3-rc-3", "index.html"), SiteUrls.versionFile(new VersionId("26.3-rc-3")));
        assertEquals("../../", SiteUrls.root(SiteUrls.VERSION_DEPTH));
    }

    @Test
    void linksToAVersionAreRelativeToTheSiteRoot() {
        VersionId id = new VersionId("26.3-rc-3");
        assertEquals("versions/26.3-rc-3/", SiteUrls.versionLink("", id));
        assertEquals("../../versions/26.3-rc-3/", SiteUrls.versionLink("../../", id));
    }

    @Test
    void unreservedCharactersAreKeptInALink() {
        assertEquals("1.21.5-rc1_x~y", SiteUrls.encode("1.21.5-rc1_x~y"));
    }

    @Test
    void otherCharactersArePercentEncodedAsUtf8() {
        String accented = "caf" + (char) 233;
        assertEquals("a%20b%2Bc%26d", SiteUrls.encode("a b+c&d"));
        assertEquals("caf%C3%A9", SiteUrls.encode(accented));
    }
}
