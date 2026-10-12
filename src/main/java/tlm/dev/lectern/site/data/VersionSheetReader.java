package tlm.dev.lectern.site.data;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import tlm.dev.lectern.site.json.JsonValue;
import tlm.dev.lectern.site.model.GameInfo;
import tlm.dev.lectern.site.model.PackVersion;
import tlm.dev.lectern.site.model.VersionId;
import tlm.dev.lectern.site.model.VersionKind;
import tlm.dev.lectern.site.model.VersionSheet;

public final class VersionSheetReader {

    static final long SUPPORTED_SCHEMA = 1;

    public VersionSheet read(String file, VersionId folder, JsonValue root) {
        JsonFields fields = JsonFields.root(file, root);
        fields.requireSchemaVersion(SUPPORTED_SCHEMA);
        VersionId id = versionId(fields, fields.string("id"), "id");
        if (!id.equals(folder)) {
            throw fields.problem("its id is '" + id.value() + "' but its folder is '" + folder.value() + "'");
        }
        return new VersionSheet(
                id,
                kind(fields),
                releaseTime(fields),
                optionalVersion(fields, "step_base"),
                optionalVersion(fields, "release_base"),
                fields.optionalObject("game").map(this::game));
    }

    private static VersionKind kind(JsonFields fields) {
        String label = fields.string("kind");
        return VersionKind.ofLabel(label)
                .orElseThrow(() -> fields.problemAt("kind", "is '" + label + "', which is not a known version kind"));
    }

    private static Instant releaseTime(JsonFields fields) {
        String text = fields.string("release_time");
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            throw fields.problemAt("release_time", "is '" + text + "', which is not an instant");
        }
    }

    private static Optional<VersionId> optionalVersion(JsonFields fields, String key) {
        return fields.nullableString(key).map(text -> versionId(fields, text, key));
    }

    private static VersionId versionId(JsonFields fields, String text, String key) {
        try {
            return new VersionId(text);
        } catch (IllegalArgumentException e) {
            throw fields.problemAt(key, "is not a version id: " + e.getMessage());
        }
    }

    private GameInfo game(JsonFields game) {
        return new GameInfo(game.optionalInteger("world_version"), game.optionalInteger("protocol_version"), packVersion(game));
    }

    private static Optional<PackVersion> packVersion(JsonFields game) {
        Optional<JsonValue> value = game.value("pack_version");
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(switch (value.get()) {
            case JsonValue.NumberValue _ -> new PackVersion.Single(smallInteger(game, "pack_version"));
            case JsonValue.ObjectValue _ -> packObject(game.object("pack_version"));
            default -> throw game.problemAt("pack_version", "is neither an integer nor an object");
        });
    }

    private static PackVersion packObject(JsonFields pack) {
        if (pack.has("data_major") && pack.has("data_minor") && pack.has("resource_major") && pack.has("resource_minor")) {
            return new PackVersion.Versioned(
                    smallInteger(pack, "data_major"),
                    smallInteger(pack, "data_minor"),
                    smallInteger(pack, "resource_major"),
                    smallInteger(pack, "resource_minor"));
        }
        if (pack.has("data") && pack.has("resource")) {
            return new PackVersion.Split(smallInteger(pack, "data"), smallInteger(pack, "resource"));
        }
        throw pack.problem("'game.pack_version' must have data and resource, or data_major, data_minor, resource_major and resource_minor");
    }

    private static int smallInteger(JsonFields fields, String key) {
        long value = fields.integer(key);
        if (value > Integer.MAX_VALUE) {
            throw fields.problemAt(key, "is too large: " + value);
        }
        return (int) value;
    }
}
