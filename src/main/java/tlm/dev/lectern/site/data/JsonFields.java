package tlm.dev.lectern.site.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.regex.Pattern;
import tlm.dev.lectern.site.json.JsonValue;

final class JsonFields {

    private static final Pattern NON_NEGATIVE_INTEGER = Pattern.compile("0|[1-9][0-9]*");

    private final String file;
    private final String path;
    private final JsonValue.ObjectValue object;

    private JsonFields(String file, String path, JsonValue.ObjectValue object) {
        this.file = file;
        this.path = path;
        this.object = object;
    }

    static JsonFields root(String file, JsonValue value) {
        if (value instanceof JsonValue.ObjectValue object) {
            return new JsonFields(file, "", object);
        }
        throw new DataFormatException(file, "the document is not a JSON object");
    }

    DataFormatException problem(String message) {
        return new DataFormatException(file, message);
    }

    DataFormatException problemAt(String key, String message) {
        return problem(describe(key) + " " + message);
    }

    Map<String, JsonValue> members() {
        return object.members();
    }

    void requireSchemaVersion(long supported) {
        long found = integer("schema_version");
        if (found > supported) {
            throw problem("its schema_version is " + found + ", newer than the " + supported + " this site reads");
        }
        if (found < 1) {
            throw problem("its schema_version is " + found + ", expected " + supported);
        }
    }

    Optional<JsonValue> value(String key) {
        return object.member(key);
    }

    String string(String key) {
        JsonValue value = required(key);
        if (value instanceof JsonValue.StringValue text) {
            return text.value();
        }
        throw problem(describe(key) + " is not a string");
    }

    Optional<String> nullableString(String key) {
        JsonValue value = required(key);
        return switch (value) {
            case JsonValue.NullValue _ -> Optional.empty();
            case JsonValue.StringValue text -> Optional.of(text.value());
            default -> throw problem(describe(key) + " is neither a string nor null");
        };
    }

    long integer(String key) {
        return integerOf(key, required(key));
    }

    OptionalLong optionalInteger(String key) {
        return object.member(key).map(value -> OptionalLong.of(integerOf(key, value))).orElse(OptionalLong.empty());
    }

    JsonFields object(String key) {
        return objectOf(key, required(key));
    }

    Optional<JsonFields> optionalObject(String key) {
        return object.member(key).map(value -> objectOf(key, value));
    }

    List<JsonValue> array(String key) {
        if (required(key) instanceof JsonValue.ArrayValue array) {
            return array.elements();
        }
        throw problem(describe(key) + " is not an array");
    }

    boolean has(String key) {
        return object.members().containsKey(key);
    }

    JsonFields element(String key, int index, JsonValue value) {
        if (value instanceof JsonValue.ObjectValue element) {
            return new JsonFields(file, path + key + "[" + index + "].", element);
        }
        throw problem(describe(key) + " element " + index + " is not an object");
    }

    private JsonFields objectOf(String key, JsonValue value) {
        if (value instanceof JsonValue.ObjectValue nested) {
            return new JsonFields(file, path + key + ".", nested);
        }
        throw problem(describe(key) + " is not an object");
    }

    private long integerOf(String key, JsonValue value) {
        if (value instanceof JsonValue.NumberValue number && NON_NEGATIVE_INTEGER.matcher(number.text()).matches()) {
            try {
                return Long.parseLong(number.text());
            } catch (NumberFormatException e) {
                throw problem(describe(key) + " is too large: " + number.text());
            }
        }
        throw problem(describe(key) + " is not a non-negative integer");
    }

    private JsonValue required(String key) {
        return object.member(key).orElseThrow(() -> problem("it has no " + describe(key)));
    }

    private String describe(String key) {
        return "'" + path + key + "'";
    }
}
