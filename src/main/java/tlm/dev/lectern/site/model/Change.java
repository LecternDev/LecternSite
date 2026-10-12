package tlm.dev.lectern.site.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public record Change(ChangeType type, Map<String, ChangeValue> subject, Map<String, ChangeValue> payload) {

    public Change {
        Objects.requireNonNull(type, "type");
        subject = Collections.unmodifiableMap(new TreeMap<>(subject));
        payload = Collections.unmodifiableMap(new TreeMap<>(payload));
    }

    public Optional<ChangeValue> value(String key) {
        ChangeValue found = subject.get(key);
        return Optional.ofNullable(found != null ? found : payload.get(key));
    }
}
