package tlm.dev.lectern.site.fixtures;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tlm.dev.lectern.site.model.Change;
import tlm.dev.lectern.site.model.ChangeSet;
import tlm.dev.lectern.site.model.ChangeType;
import tlm.dev.lectern.site.model.ChangeValue;
import tlm.dev.lectern.site.model.SkippedDomain;
import tlm.dev.lectern.site.model.VersionId;
import tlm.dev.lectern.site.model.VersionKind;
import tlm.dev.lectern.site.model.VersionRef;

public final class ChangeFixtures {

    private ChangeFixtures() {
    }

    public static Change change(String type, Object... keysAndValues) {
        Map<String, ChangeValue> payload = new TreeMap<>();
        for (int index = 0; index < keysAndValues.length; index += 2) {
            payload.put((String) keysAndValues[index], value(keysAndValues[index + 1]));
        }
        return new Change(new ChangeType(type), Map.of(), payload);
    }

    public static ChangeValue value(Object raw) {
        return switch (raw) {
            case null -> new ChangeValue.Nothing();
            case String text -> new ChangeValue.Text(text);
            case Integer number -> new ChangeValue.Numeric(number.toString());
            case Boolean flag -> new ChangeValue.Flag(flag);
            case List<?> items -> new ChangeValue.Items(items.stream().map(ChangeFixtures::value).toList());
            case Map<?, ?> fields -> new ChangeValue.Fields(fieldValues(fields));
            default -> throw new IllegalArgumentException("Unsupported fixture value " + raw);
        };
    }

    public static ChangeSet changeSet(String from, String to, List<SkippedDomain> skipped, List<Change> changes) {
        return new ChangeSet(
                new VersionRef(new VersionId(from), new VersionKind.Release()),
                new VersionRef(new VersionId(to), new VersionKind.Release()),
                skipped,
                changes);
    }

    private static Map<String, ChangeValue> fieldValues(Map<?, ?> fields) {
        Map<String, ChangeValue> values = new TreeMap<>();
        fields.forEach((name, member) -> values.put((String) name, value(member)));
        return values;
    }
}
