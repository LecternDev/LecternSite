package tlm.dev.lectern.site.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ChangeTypeTest {

    @Test
    void domainIsTheTextBeforeTheFirstDot() {
        assertEquals("registry", new ChangeType("registry.entry_added").domain());
        assertEquals("recipe", new ChangeType("recipe.ingredient.added").domain());
    }

    @Test
    void nameWithoutADotIsItsOwnDomain() {
        assertEquals("standalone", new ChangeType("standalone").domain());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "registry. added", "registry\tadded"})
    void blankOrSpacedNameIsRejected(String name) {
        assertThrows(IllegalArgumentException.class, () -> new ChangeType(name));
    }
}
