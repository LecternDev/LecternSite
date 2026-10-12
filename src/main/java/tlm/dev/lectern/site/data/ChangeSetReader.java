package tlm.dev.lectern.site.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import tlm.dev.lectern.site.json.JsonValue;
import tlm.dev.lectern.site.model.Change;
import tlm.dev.lectern.site.model.ChangeSet;
import tlm.dev.lectern.site.model.ChangeType;
import tlm.dev.lectern.site.model.ChangeValue;
import tlm.dev.lectern.site.model.SkippedDomain;
import tlm.dev.lectern.site.model.SkippedSide;
import tlm.dev.lectern.site.model.VersionId;
import tlm.dev.lectern.site.model.VersionKind;
import tlm.dev.lectern.site.model.VersionRef;

public final class ChangeSetReader {

    static final long SUPPORTED_SCHEMA = 1;

    private static final Set<String> SUBJECT_KEYS =
            Set.of("registry", "entry", "block", "property", "phase", "direction", "packet", "command", "path");

    public ChangeSet read(String file, JsonValue root) {
        JsonFields fields = JsonFields.root(file, root);
        fields.requireSchemaVersion(SUPPORTED_SCHEMA);
        return new ChangeSet(
                versionRef(fields.object("from")),
                versionRef(fields.object("to")),
                skipped(fields),
                changes(fields));
    }

    private static VersionRef versionRef(JsonFields fields) {
        String idText = fields.string("id");
        VersionId id;
        try {
            id = new VersionId(idText);
        } catch (IllegalArgumentException e) {
            throw fields.problemAt("id", "is not a version id: " + e.getMessage());
        }
        String label = fields.string("kind");
        VersionKind kind = VersionKind.ofLabel(label)
                .orElseThrow(() -> fields.problemAt("kind", "is '" + label + "', which is not a known version kind"));
        return new VersionRef(id, kind);
    }

    private static List<SkippedDomain> skipped(JsonFields fields) {
        List<SkippedDomain> skipped = new ArrayList<>();
        List<JsonValue> elements = fields.array("skipped");
        for (int index = 0; index < elements.size(); index++) {
            JsonFields element = fields.element("skipped", index, elements.get(index));
            String sideLabel = element.string("side");
            SkippedSide side = SkippedSide.ofLabel(sideLabel)
                    .orElseThrow(() -> element.problemAt("side", "is '" + sideLabel + "', expected from, to or both"));
            skipped.add(new SkippedDomain(element.string("domain"), side, element.string("reason")));
        }
        return skipped;
    }

    private static List<Change> changes(JsonFields fields) {
        List<Change> changes = new ArrayList<>();
        List<JsonValue> elements = fields.array("changes");
        for (int index = 0; index < elements.size(); index++) {
            changes.add(change(fields.element("changes", index, elements.get(index))));
        }
        return changes;
    }

    private static Change change(JsonFields element) {
        ChangeType type;
        try {
            type = new ChangeType(element.string("type"));
        } catch (IllegalArgumentException e) {
            throw element.problemAt("type", "is not a change type: " + e.getMessage());
        }
        Map<String, ChangeValue> subject = new TreeMap<>();
        Map<String, ChangeValue> payload = new TreeMap<>();
        for (Map.Entry<String, JsonValue> member : element.members().entrySet()) {
            if (member.getKey().equals("type")) {
                continue;
            }
            Map<String, ChangeValue> target = SUBJECT_KEYS.contains(member.getKey()) ? subject : payload;
            target.put(member.getKey(), value(member.getValue()));
        }
        return new Change(type, subject, payload);
    }

    private static ChangeValue value(JsonValue json) {
        return switch (json) {
            case JsonValue.StringValue text -> new ChangeValue.Text(text.value());
            case JsonValue.NumberValue number -> new ChangeValue.Numeric(number.text());
            case JsonValue.BooleanValue flag -> new ChangeValue.Flag(flag.value());
            case JsonValue.NullValue _ -> new ChangeValue.Nothing();
            case JsonValue.ArrayValue array -> new ChangeValue.Items(array.elements().stream().map(ChangeSetReader::value).toList());
            case JsonValue.ObjectValue object -> new ChangeValue.Fields(fieldValues(object.members()));
        };
    }

    private static Map<String, ChangeValue> fieldValues(Map<String, JsonValue> members) {
        Map<String, ChangeValue> values = new TreeMap<>();
        members.forEach((name, member) -> values.put(name, value(member)));
        return values;
    }
}
