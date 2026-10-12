package tlm.dev.lectern.site.model;

import java.util.List;
import java.util.Optional;

public sealed interface SkippedSide {

    List<SkippedSide> ALL = List.of(new From(), new To(), new Both());

    String label();

    static Optional<SkippedSide> ofLabel(String label) {
        return ALL.stream().filter(side -> side.label().equals(label)).findFirst();
    }

    record From() implements SkippedSide {

        @Override
        public String label() {
            return "from";
        }
    }

    record To() implements SkippedSide {

        @Override
        public String label() {
            return "to";
        }
    }

    record Both() implements SkippedSide {

        @Override
        public String label() {
            return "both";
        }
    }
}
