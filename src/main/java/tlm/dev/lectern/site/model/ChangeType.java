package tlm.dev.lectern.site.model;

import java.util.Objects;

public record ChangeType(String name) {

    public ChangeType {
        Objects.requireNonNull(name, "name");
        if (name.isBlank() || name.chars().anyMatch(character -> Character.isWhitespace(character) || Character.isISOControl(character))) {
            throw new IllegalArgumentException("A change type must be a non-blank name without whitespace: '" + name + "'");
        }
    }

    public String domain() {
        int dot = name.indexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }
}
