package tlm.dev.lectern.site.pages;

import java.util.Map;

final class DomainNames {

    private static final Map<String, String> BY_CHANGE_PREFIX =
            Map.of("registry", "Registries", "block", "Blocks", "packet", "Packets", "command", "Commands");
    private static final Map<String, String> BY_SKIPPED_DOMAIN =
            Map.of("registries", "Registries", "blocks", "Blocks", "packets", "Packets", "commands", "Commands");

    private DomainNames() {
    }

    static String forChanges(String prefix) {
        return BY_CHANGE_PREFIX.getOrDefault(prefix, capitalized(prefix));
    }

    static String forSkipped(String domain) {
        return BY_SKIPPED_DOMAIN.getOrDefault(domain, capitalized(domain));
    }

    private static String capitalized(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
