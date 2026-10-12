package tlm.dev.lectern.site.pages;

import java.util.List;

public record DomainBlock(String name, String countText, List<ChangeLine> lines, List<SentencePart> shifts) {

    public DomainBlock {
        lines = List.copyOf(lines);
        shifts = List.copyOf(shifts);
    }

    public boolean hasShifts() {
        return !shifts.isEmpty();
    }
}
