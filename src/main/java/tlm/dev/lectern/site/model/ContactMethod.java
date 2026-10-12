package tlm.dev.lectern.site.model;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public record ContactMethod(String value) {

    private static final Pattern EMAIL = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
    private static final String HTTPS = "https://";

    public ContactMethod {
        Objects.requireNonNull(value, "value");
        if (value.isBlank() || !value.equals(value.strip()) || value.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("A contact method must be non-blank text without surrounding whitespace or control characters");
        }
    }

    public Optional<String> href() {
        if (EMAIL.matcher(value).matches()) {
            return Optional.of("mailto:" + value);
        }
        if (value.startsWith(HTTPS) && value.length() > HTTPS.length() && value.chars().noneMatch(Character::isWhitespace)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }
}
