package tlm.dev.lectern.site.pages;

import java.util.List;
import java.util.Map;
import java.util.Set;

final class ChangeTypeCatalog {

    static final Set<String> SHIFT_TYPES =
            Set.of("registry.ids_shifted", "registry.entry_ids_shifted", "block.state_ids_shifted", "packet.ids_shifted");

    static final Map<String, List<String>> KEYS = Map.ofEntries(
            Map.entry("registry.ids_shifted", List.of("count", "first_id")),
            Map.entry("registry.added", List.of("registry", "protocol_id", "entry_count")),
            Map.entry("registry.removed", List.of("registry", "protocol_id", "entry_count")),
            Map.entry("registry.default_modified", List.of("registry", "old_default", "new_default")),
            Map.entry("registry.id_modified", List.of("registry", "old_id", "new_id")),
            Map.entry("registry.entry_added", List.of("registry", "entry", "protocol_id")),
            Map.entry("registry.entry_removed", List.of("registry", "entry", "protocol_id")),
            Map.entry("registry.entry_id_modified", List.of("registry", "entry", "old_id", "new_id")),
            Map.entry("registry.entry_ids_shifted", List.of("registry", "count", "first_id")),
            Map.entry("block.state_ids_shifted", List.of("state_count", "block_count", "first_id")),
            Map.entry("block.added", List.of("block", "state_count")),
            Map.entry("block.removed", List.of("block", "state_count")),
            Map.entry("block.property_added", List.of("block", "property", "values", "default")),
            Map.entry("block.property_removed", List.of("block", "property", "values", "default")),
            Map.entry("block.property_values_added", List.of("block", "property", "values")),
            Map.entry("block.property_values_removed", List.of("block", "property", "values")),
            Map.entry("block.default_state_modified", List.of("block", "old_default", "new_default")),
            Map.entry("block.state_ids_modified", List.of("block", "state_count", "old_first_id", "new_first_id")),
            Map.entry("packet.phase_added", List.of("phase", "packet_count")),
            Map.entry("packet.phase_removed", List.of("phase", "packet_count")),
            Map.entry("packet.direction_added", List.of("phase", "direction", "packet_count")),
            Map.entry("packet.direction_removed", List.of("phase", "direction", "packet_count")),
            Map.entry("packet.added", List.of("phase", "direction", "packet", "protocol_id")),
            Map.entry("packet.removed", List.of("phase", "direction", "packet", "protocol_id")),
            Map.entry("packet.id_modified", List.of("phase", "direction", "packet", "old_id", "new_id")),
            Map.entry("packet.ids_shifted", List.of("phase", "direction", "count", "first_id")),
            Map.entry("command.added", List.of("command", "kind", "node_count")),
            Map.entry("command.removed", List.of("command", "kind", "node_count")),
            Map.entry("command.node_added", List.of("path", "kind", "node_count")),
            Map.entry("command.node_removed", List.of("path", "kind", "node_count")),
            Map.entry("command.parser_modified", List.of("path", "old_parser", "new_parser")),
            Map.entry("command.properties_modified", List.of("path", "old_properties", "new_properties")),
            Map.entry("command.executable_modified", List.of("path", "old_executable", "new_executable")),
            Map.entry("command.permissions_modified", List.of("path", "old_permissions", "new_permissions")),
            Map.entry("command.redirect_modified", List.of("path", "old_redirect", "new_redirect")));

    private ChangeTypeCatalog() {
    }
}
