package tlm.dev.lectern.site.pages;

import static org.junit.jupiter.api.Assertions.fail;

final class HtmlRows {

    static final String UNSTABLE_LABEL = ">unstable<";

    private HtmlRows() {
    }

    static String rowOf(String page, String versionId) {
        String opening = "<tr data-version=\"" + versionId + "\">";
        int start = page.indexOf(opening);
        if (start < 0) {
            return fail("No row for version " + versionId + " in page:\n" + page);
        }
        return page.substring(start, page.indexOf("</tr>", start));
    }

    static int countOf(String text, String fragment) {
        int count = 0;
        for (int index = text.indexOf(fragment); index >= 0; index = text.indexOf(fragment, index + fragment.length())) {
            count++;
        }
        return count;
    }
}
