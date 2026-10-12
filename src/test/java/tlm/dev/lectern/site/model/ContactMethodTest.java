package tlm.dev.lectern.site.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ContactMethodTest {

    @Test
    void emailBecomesAMailtoLink() {
        assertEquals(Optional.of("mailto:team@example.org"), new ContactMethod("team@example.org").href());
    }

    @Test
    void httpsAddressBecomesALink() {
        assertEquals(Optional.of("https://example.org/contact"), new ContactMethod("https://example.org/contact").href());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://example.org/contact", "javascript:alert(1)", "https://", "write to the maintainers", "not an@email"})
    void anythingElseStaysPlainText(String value) {
        assertEquals(Optional.empty(), new ContactMethod(value).href());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", " team@example.org", "team@example.org ", "team@example.org\n"})
    void blankOrPaddedValueIsRejected(String value) {
        assertThrows(IllegalArgumentException.class, () -> new ContactMethod(value));
    }
}
