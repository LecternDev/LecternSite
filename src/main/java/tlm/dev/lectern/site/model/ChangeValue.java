package tlm.dev.lectern.site.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public sealed interface ChangeValue {

    record Text(String value) implements ChangeValue {

        public Text {
            Objects.requireNonNull(value, "value");
        }
    }

    record Numeric(String text) implements ChangeValue {

        public Numeric {
            Objects.requireNonNull(text, "text");
        }
    }

    record Flag(boolean value) implements ChangeValue {
    }

    record Nothing() implements ChangeValue {
    }

    record Items(List<ChangeValue> items) implements ChangeValue {

        public Items {
            items = List.copyOf(items);
        }
    }

    record Fields(Map<String, ChangeValue> fields) implements ChangeValue {

        public Fields {
            fields = Collections.unmodifiableMap(new TreeMap<>(fields));
        }
    }
}
