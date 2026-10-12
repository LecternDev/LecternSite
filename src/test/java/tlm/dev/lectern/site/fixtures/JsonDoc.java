package tlm.dev.lectern.site.fixtures;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class JsonDoc {

    private final Map<String, String> members;

    private JsonDoc(Map<String, String> members) {
        this.members = members;
    }

    public static JsonDoc of(String... keysAndRawValues) {
        Map<String, String> members = new LinkedHashMap<>();
        for (int index = 0; index < keysAndRawValues.length; index += 2) {
            members.put(keysAndRawValues[index], keysAndRawValues[index + 1]);
        }
        return new JsonDoc(members);
    }

    public JsonDoc with(String key, String rawValue) {
        Map<String, String> copy = new LinkedHashMap<>(members);
        copy.put(key, rawValue);
        return new JsonDoc(copy);
    }

    public JsonDoc without(String key) {
        Map<String, String> copy = new LinkedHashMap<>(members);
        copy.remove(key);
        return new JsonDoc(copy);
    }

    public String text() {
        return members.entrySet().stream()
                .map(member -> "\"" + member.getKey() + "\": " + member.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
