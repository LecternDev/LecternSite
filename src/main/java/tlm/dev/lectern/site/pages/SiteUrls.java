package tlm.dev.lectern.site.pages;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import tlm.dev.lectern.site.model.VersionId;

final class SiteUrls {

    static final Path HOME_FILE = Path.of("index.html");
    static final int HOME_DEPTH = 0;
    static final int VERSION_DEPTH = 2;

    private static final String VERSIONS = "versions";
    private static final String INDEX = "index.html";
    private static final String UNRESERVED = "-._~";
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private SiteUrls() {
    }

    static Path versionFile(VersionId id) {
        return Path.of(VERSIONS, id.value(), INDEX);
    }

    static String root(int depth) {
        return "../".repeat(depth);
    }

    static String versionLink(String root, VersionId id) {
        return root + VERSIONS + "/" + encode(id.value()) + "/";
    }

    static String encode(String segment) {
        StringBuilder encoded = new StringBuilder();
        for (byte value : segment.getBytes(StandardCharsets.UTF_8)) {
            char character = (char) (value & 0xFF);
            if (isUnreserved(character)) {
                encoded.append(character);
            } else {
                encoded.append('%').append(HEX[character >> 4]).append(HEX[character & 0x0F]);
            }
        }
        return encoded.toString();
    }

    private static boolean isUnreserved(char character) {
        boolean letterOrDigit = character < 128 && Character.isLetterOrDigit(character);
        return letterOrDigit || UNRESERVED.indexOf(character) >= 0;
    }
}
