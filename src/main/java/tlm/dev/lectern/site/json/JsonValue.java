package tlm.dev.lectern.site.json;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

public sealed interface JsonValue {

    record ObjectValue(Map<String, JsonValue> members) implements JsonValue {

        public ObjectValue {
            members = Collections.unmodifiableMap(new TreeMap<>(members));
        }

        public Optional<JsonValue> member(String name) {
            return Optional.ofNullable(members.get(name));
        }
    }

    record ArrayValue(List<JsonValue> elements) implements JsonValue {

        public ArrayValue {
            elements = List.copyOf(elements);
        }
    }

    record StringValue(String value) implements JsonValue {

        public StringValue {
            Objects.requireNonNull(value, "value");
        }
    }

    record NumberValue(String text) implements JsonValue {

        private static final Pattern SHAPE = Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?");

        public NumberValue {
            Objects.requireNonNull(text, "text");
            if (!SHAPE.matcher(text).matches()) {
                throw new IllegalArgumentException("Not a JSON number: '" + text + "'");
            }
        }
    }

    record BooleanValue(boolean value) implements JsonValue {
    }

    record NullValue() implements JsonValue {
    }
}
