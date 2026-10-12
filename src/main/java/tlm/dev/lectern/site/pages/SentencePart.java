package tlm.dev.lectern.site.pages;

import java.util.Objects;

public record SentencePart(String text, boolean value) {

    public SentencePart {
        Objects.requireNonNull(text, "text");
    }
}
