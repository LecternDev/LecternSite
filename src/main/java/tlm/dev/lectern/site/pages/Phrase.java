package tlm.dev.lectern.site.pages;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

record Phrase(List<Segment> segments, boolean shift) {

    sealed interface Segment {
    }

    record Literal(String text) implements Segment {
    }

    record Slot(String key) implements Segment {
    }

    Phrase {
        segments = List.copyOf(segments);
    }

    static Phrase sentence(String pattern) {
        return new Phrase(parse(pattern), false);
    }

    static Phrase shift(String pattern) {
        return new Phrase(parse(pattern), true);
    }

    Set<String> keys() {
        Set<String> keys = new LinkedHashSet<>();
        for (Segment segment : segments) {
            if (segment instanceof Slot slot) {
                keys.add(slot.key());
            }
        }
        return keys;
    }

    private static List<Segment> parse(String pattern) {
        List<Segment> segments = new ArrayList<>();
        int position = 0;
        while (position < pattern.length()) {
            int open = pattern.indexOf('{', position);
            if (open < 0) {
                segments.add(new Literal(pattern.substring(position)));
                break;
            }
            int close = pattern.indexOf('}', open);
            if (close < 0) {
                throw new IllegalArgumentException("Unclosed slot in phrase: " + pattern);
            }
            if (open > position) {
                segments.add(new Literal(pattern.substring(position, open)));
            }
            segments.add(new Slot(pattern.substring(open + 1, close)));
            position = close + 1;
        }
        return segments;
    }
}
