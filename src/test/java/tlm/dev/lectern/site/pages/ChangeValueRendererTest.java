package tlm.dev.lectern.site.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tlm.dev.lectern.site.fixtures.ChangeFixtures.value;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ChangeValueRendererTest {

    private final ChangeValueRenderer renderer = new ChangeValueRenderer();

    @Test
    void scalarsAreShownAsWritten() {
        assertEquals("minecraft:stone", renderer.render("block", value("minecraft:stone")));
        assertEquals("12", renderer.render("count", value(12)));
        assertEquals("true", renderer.render("executable", value(true)));
        assertEquals("false", renderer.render("executable", value(false)));
    }

    @Test
    void nullIsShownAsNone() {
        assertEquals("none", renderer.render("old_default", value(null)));
    }

    @Test
    void listIsJoinedWithCommas() {
        assertEquals("north, south, east", renderer.render("values", value(List.of("north", "south", "east"))));
    }

    @Test
    void pathIsJoinedWithSpaces() {
        assertEquals("execute as targets", renderer.render("path", value(List.of("execute", "as", "targets"))));
    }

    @Test
    void objectIsShownAsKeyValuePairs() {
        assertEquals("facing=north, open=true", renderer.render("old_default", value(Map.of("facing", "north", "open", "true"))));
    }

    @Test
    void emptyObjectAndEmptyListAreShownAsNone() {
        assertEquals("none", renderer.render("old_default", value(Map.of())));
        assertEquals("none", renderer.render("values", value(List.of())));
    }

    @Test
    void nestedValuesAreBracketed() {
        Map<String, Object> permissions = Map.of("permission", Map.of("level", 2, "type", "demo:level"), "type", "demo:required");
        assertEquals("permission={level=2, type=demo:level}, type=demo:required", renderer.render("new_permissions", value(permissions)));
        assertEquals("a=[1, 2]", renderer.render("x", value(Map.of("a", List.of(1, 2)))));
    }
}
