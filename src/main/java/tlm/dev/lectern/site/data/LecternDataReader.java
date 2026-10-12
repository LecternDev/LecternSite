package tlm.dev.lectern.site.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import tlm.dev.lectern.site.json.JsonReader;
import tlm.dev.lectern.site.json.JsonSyntaxException;
import tlm.dev.lectern.site.json.JsonValue;
import tlm.dev.lectern.site.model.ChangeSet;
import tlm.dev.lectern.site.model.SiteData;
import tlm.dev.lectern.site.model.VersionData;
import tlm.dev.lectern.site.model.VersionId;
import tlm.dev.lectern.site.model.VersionSheet;

public final class LecternDataReader {

    private static final String VERSIONS = "versions";
    private static final String DIFFS = "diffs";
    private static final String SHEET = "meta.json";
    private static final String STEP = "step.json";
    private static final String RELEASE = "release.json";

    private final Path root;
    private final JsonReader json;
    private final VersionSheetReader sheets;
    private final ChangeSetReader changeSets;

    public LecternDataReader(Path root, JsonReader json, VersionSheetReader sheets, ChangeSetReader changeSets) {
        this.root = root;
        this.json = json;
        this.sheets = sheets;
        this.changeSets = changeSets;
    }

    public SiteData read() {
        Path versionsFolder = root.resolve(VERSIONS);
        if (!Files.isDirectory(versionsFolder)) {
            throw new DataFormatException(root.toString(), "this is not a LecternData clone: it has no " + VERSIONS + " folder");
        }
        Map<VersionId, VersionSheet> found = readSheets(versionsFolder);
        rejectOrphanDiffs(found.keySet());
        List<VersionData> versions = new ArrayList<>();
        for (VersionSheet sheet : found.values()) {
            versions.add(new VersionData(
                    sheet,
                    changeSet(sheet, STEP, sheet.stepBase(), "step_base"),
                    changeSet(sheet, RELEASE, sheet.releaseBase(), "release_base")));
        }
        return new SiteData(versions);
    }

    private Map<VersionId, VersionSheet> readSheets(Path versionsFolder) {
        Map<VersionId, VersionSheet> found = new LinkedHashMap<>();
        for (Path folder : entries(versionsFolder)) {
            VersionId id = folderId(folder);
            Path file = folder.resolve(SHEET);
            String label = label(file);
            if (!Files.isRegularFile(file)) {
                throw new DataFormatException(label, "is missing");
            }
            found.put(id, sheets.read(label, id, readJson(file, label)));
        }
        return found;
    }

    private void rejectOrphanDiffs(Set<VersionId> published) {
        Path diffsFolder = root.resolve(DIFFS);
        if (!Files.isDirectory(diffsFolder)) {
            return;
        }
        for (Path folder : entries(diffsFolder)) {
            if (!published.contains(folderId(folder))) {
                throw new DataFormatException(label(folder), "has no matching " + VERSIONS + "/" + folder.getFileName() + "/" + SHEET);
            }
        }
    }

    private Optional<ChangeSet> changeSet(VersionSheet sheet, String fileName, Optional<VersionId> base, String baseKey) {
        Path file = root.resolve(DIFFS).resolve(sheet.id().value()).resolve(fileName);
        String label = label(file);
        boolean exists = Files.isRegularFile(file);
        if (base.isEmpty()) {
            if (exists) {
                throw new DataFormatException(label, "exists but " + label(sheetFile(sheet)) + " has no " + baseKey);
            }
            return Optional.empty();
        }
        if (!exists) {
            throw new DataFormatException(label, "is missing but " + label(sheetFile(sheet)) + " names " + baseKey + " '" + base.get().value() + "'");
        }
        ChangeSet changes = changeSets.read(label, readJson(file, label));
        requireConsistent(label, sheet, base.get(), changes);
        return Optional.of(changes);
    }

    private static void requireConsistent(String label, VersionSheet sheet, VersionId base, ChangeSet changes) {
        if (!changes.to().id().equals(sheet.id())) {
            throw new DataFormatException(label, "it ends at '" + changes.to().id().value() + "' but its folder is '" + sheet.id().value() + "'");
        }
        if (!changes.to().kind().equals(sheet.kind())) {
            throw new DataFormatException(label, "its 'to' kind is '" + changes.to().kind().label() + "' but the version sheet says '" + sheet.kind().label() + "'");
        }
        if (!changes.from().id().equals(base)) {
            throw new DataFormatException(label, "it starts at '" + changes.from().id().value() + "' but the version sheet names '" + base.value() + "'");
        }
    }

    private Path sheetFile(VersionSheet sheet) {
        return root.resolve(VERSIONS).resolve(sheet.id().value()).resolve(SHEET);
    }

    private VersionId folderId(Path folder) {
        String label = label(folder);
        if (!Files.isDirectory(folder)) {
            throw new DataFormatException(label, "is not a folder");
        }
        try {
            return new VersionId(folder.getFileName().toString());
        } catch (IllegalArgumentException e) {
            throw new DataFormatException(label, "its name is not a version id: " + e.getMessage(), e);
        }
    }

    private List<Path> entries(Path folder) {
        try (Stream<Path> children = Files.list(folder)) {
            return children.sorted().toList();
        } catch (IOException e) {
            throw new DataFormatException(label(folder), "cannot be listed: " + e.getMessage(), e);
        }
    }

    private JsonValue readJson(Path file, String label) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(file);
        } catch (IOException e) {
            throw new DataFormatException(label, "cannot be read: " + e.getMessage(), e);
        }
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            throw new DataFormatException(label, "is not valid UTF-8", e);
        }
        try {
            return json.read(text);
        } catch (JsonSyntaxException e) {
            throw new DataFormatException(label, "is not valid JSON: " + e.getMessage(), e);
        }
    }

    private String label(Path path) {
        return root.relativize(path).toString().replace('\\', '/');
    }
}
