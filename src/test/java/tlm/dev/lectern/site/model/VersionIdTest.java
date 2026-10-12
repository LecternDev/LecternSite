package tlm.dev.lectern.site.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VersionIdTest {

    @ParameterizedTest
    @ValueSource(strings = {"1.21.5", "25w14craftmine", "1.21.5-rc1", "3D Shareware v1.34", "1.14 Pre-Release 1"})
    void manifestIdsAreKeptVerbatim(String id) {
        assertEquals(id, new VersionId(id).value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", " 1.21.5", "1.21.5 "})
    void blankOrPaddedIdIsRejected(String id) {
        assertThrows(IllegalArgumentException.class, () -> new VersionId(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.21/5", "1.21\\5", "1.21\n5", "1.21\t5"})
    void idWithAPathSeparatorOrControlCharacterIsRejected(String id) {
        assertThrows(IllegalArgumentException.class, () -> new VersionId(id));
    }

    @Test
    void nullIdIsRejected() {
        assertThrows(NullPointerException.class, () -> new VersionId(null));
    }
}
