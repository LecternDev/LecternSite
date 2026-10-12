package tlm.dev.lectern.site.pages;

import java.util.List;

public record ChangeLine(List<SentencePart> parts, boolean shift) {

    public ChangeLine {
        parts = List.copyOf(parts);
    }

    public String plainText() {
        StringBuilder text = new StringBuilder();
        parts.forEach(part -> text.append(part.text()));
        return text.toString();
    }
}
