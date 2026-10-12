package tlm.dev.lectern.site.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tlm.dev.lectern.site.model.Change;
import tlm.dev.lectern.site.model.ChangeValue;

public final class ChangeSentenceService {

    private static final Map<String, Phrase> PHRASES = Map.ofEntries(
            Map.entry("registry.ids_shifted", Phrase.shift("registries: {count} from id {first_id}")),
            Map.entry("registry.added", Phrase.sentence("Registry {registry} was added (protocol id {protocol_id}, entries: {entry_count}).")),
            Map.entry("registry.removed", Phrase.sentence("Registry {registry} was removed (protocol id {protocol_id}, entries: {entry_count}).")),
            Map.entry("registry.default_modified", Phrase.sentence("The default entry of registry {registry} changed from {old_default} to {new_default}.")),
            Map.entry("registry.id_modified", Phrase.sentence("Registry {registry} changed its protocol id from {old_id} to {new_id}.")),
            Map.entry("registry.entry_added", Phrase.sentence("Entry {entry} was added to registry {registry} (id {protocol_id}).")),
            Map.entry("registry.entry_removed", Phrase.sentence("Entry {entry} was removed from registry {registry} (id {protocol_id}).")),
            Map.entry("registry.entry_id_modified", Phrase.sentence("Entry {entry} of registry {registry} changed id from {old_id} to {new_id}.")),
            Map.entry("registry.entry_ids_shifted", Phrase.shift("{registry} entries: {count} from id {first_id}")),
            Map.entry("block.state_ids_shifted", Phrase.shift("block states: {state_count} (blocks: {block_count}) from id {first_id}")),
            Map.entry("block.added", Phrase.sentence("Block {block} was added (states: {state_count}).")),
            Map.entry("block.removed", Phrase.sentence("Block {block} was removed (states: {state_count}).")),
            Map.entry("block.property_added", Phrase.sentence("Block {block} gained property {property} (values: {values}, default: {default}).")),
            Map.entry("block.property_removed", Phrase.sentence("Block {block} lost property {property} (values: {values}, default: {default}).")),
            Map.entry("block.property_values_added", Phrase.sentence("Property {property} of block {block} gained values: {values}.")),
            Map.entry("block.property_values_removed", Phrase.sentence("Property {property} of block {block} lost values: {values}.")),
            Map.entry("block.default_state_modified", Phrase.sentence("The default state of block {block} changed from {old_default} to {new_default}.")),
            Map.entry("block.state_ids_modified", Phrase.sentence("Block {block} changed the ids of some states (states: {state_count}, lowest id from {old_first_id} to {new_first_id}).")),
            Map.entry("packet.phase_added", Phrase.sentence("Phase {phase} was added (packets: {packet_count}).")),
            Map.entry("packet.phase_removed", Phrase.sentence("Phase {phase} was removed (packets: {packet_count}).")),
            Map.entry("packet.direction_added", Phrase.sentence("Phase {phase} gained the {direction} direction (packets: {packet_count}).")),
            Map.entry("packet.direction_removed", Phrase.sentence("Phase {phase} lost the {direction} direction (packets: {packet_count}).")),
            Map.entry("packet.added", Phrase.sentence("Packet {packet} was added to {phase} {direction} (id {protocol_id}).")),
            Map.entry("packet.removed", Phrase.sentence("Packet {packet} was removed from {phase} {direction} (id {protocol_id}).")),
            Map.entry("packet.id_modified", Phrase.sentence("Packet {packet} of {phase} {direction} changed id from {old_id} to {new_id}.")),
            Map.entry("packet.ids_shifted", Phrase.shift("{phase} {direction} packets: {count} from id {first_id}")),
            Map.entry("command.added", Phrase.sentence("Command {command} was added ({kind}, nodes: {node_count}).")),
            Map.entry("command.removed", Phrase.sentence("Command {command} was removed ({kind}, nodes: {node_count}).")),
            Map.entry("command.node_added", Phrase.sentence("Node {path} was added ({kind}, nodes: {node_count}).")),
            Map.entry("command.node_removed", Phrase.sentence("Node {path} was removed ({kind}, nodes: {node_count}).")),
            Map.entry("command.parser_modified", Phrase.sentence("The parser of {path} changed from {old_parser} to {new_parser}.")),
            Map.entry("command.properties_modified", Phrase.sentence("The properties of {path} changed from {old_properties} to {new_properties}.")),
            Map.entry("command.executable_modified", Phrase.sentence("The executable flag of {path} changed from {old_executable} to {new_executable}.")),
            Map.entry("command.permissions_modified", Phrase.sentence("The permissions of {path} changed from {old_permissions} to {new_permissions}.")),
            Map.entry("command.redirect_modified", Phrase.sentence("The redirect of {path} changed from {old_redirect} to {new_redirect}.")));

    private final ChangeValueRenderer values;

    public ChangeSentenceService(ChangeValueRenderer values) {
        this.values = values;
    }

    public static Set<String> knownTypes() {
        return PHRASES.keySet();
    }

    static Set<String> keysOf(String type) {
        return PHRASES.get(type).keys();
    }

    public ChangeLine line(Change change) {
        Phrase phrase = PHRASES.get(change.type().name());
        if (phrase != null) {
            Optional<List<SentencePart>> parts = fill(phrase, change);
            if (parts.isPresent()) {
                return new ChangeLine(parts.get(), phrase.shift());
            }
        }
        return new ChangeLine(generic(change), false);
    }

    private Optional<List<SentencePart>> fill(Phrase phrase, Change change) {
        List<SentencePart> parts = new ArrayList<>();
        for (Phrase.Segment segment : phrase.segments()) {
            switch (segment) {
                case Phrase.Literal literal -> parts.add(new SentencePart(literal.text(), false));
                case Phrase.Slot slot -> {
                    Optional<ChangeValue> value = change.value(slot.key());
                    if (value.isEmpty()) {
                        return Optional.empty();
                    }
                    parts.add(new SentencePart(values.render(slot.key(), value.get()), true));
                }
            }
        }
        return Optional.of(parts);
    }

    private List<SentencePart> generic(Change change) {
        List<SentencePart> parts = new ArrayList<>();
        parts.add(new SentencePart("Change ", false));
        parts.add(new SentencePart(change.type().name(), true));
        boolean first = true;
        for (Map<String, ChangeValue> fields : List.of(change.subject(), change.payload())) {
            for (Map.Entry<String, ChangeValue> field : fields.entrySet()) {
                parts.add(new SentencePart(first ? ": " : ", ", false));
                parts.add(new SentencePart(field.getKey() + " ", false));
                parts.add(new SentencePart(values.render(field.getKey(), field.getValue()), true));
                first = false;
            }
        }
        parts.add(new SentencePart(".", false));
        return parts;
    }
}
