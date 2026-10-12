package tlm.dev.lectern.site;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tlm.dev.lectern.site.model.ContactMethod;

class CommandLineTest {

    private static CommandLine parse(String... args) {
        return CommandLine.parse(args);
    }

    private static UsageException failure(String... args) {
        return assertThrows(UsageException.class, () -> CommandLine.parse(args));
    }

    @Test
    void dataAloneUsesTheDefaultOutputAndNoContact() {
        CommandLine command = parse("--data", "clone");
        assertEquals(Path.of("clone"), command.data());
        assertEquals(Path.of("build/site"), command.out());
        assertEquals(Optional.empty(), command.contact());
        assertFalse(command.publicBuild());
    }

    @Test
    void everyOptionCanBeGivenInAnyOrder() {
        CommandLine command = parse("--public", "--contact", "team@example.org", "--out", "site", "--data", "clone");
        assertEquals(Path.of("clone"), command.data());
        assertEquals(Path.of("site"), command.out());
        assertEquals(Optional.of(new ContactMethod("team@example.org")), command.contact());
        assertTrue(command.publicBuild());
    }

    @Test
    void contactWithoutPublicIsAccepted() {
        assertEquals(Optional.of(new ContactMethod("https://example.org/contact")), parse("--data", "clone", "--contact", "https://example.org/contact").contact());
    }

    @Test
    void publicWithoutContactStopsTheGeneration() {
        UsageException failure = failure("--data", "clone", "--public");
        assertTrue(failure.getMessage().contains("--public needs --contact"), failure.getMessage());
    }

    @Test
    void missingDataIsAUsageError() {
        assertTrue(failure().getMessage().contains("missing option --data"));
        assertTrue(failure("--out", "site").getMessage().contains("missing option --data"));
    }

    @Test
    void unknownOptionIsAUsageError() {
        assertTrue(failure("--data", "clone", "--verbose").getMessage().contains("unknown option --verbose"));
    }

    @Test
    void positionalArgumentIsAUsageError() {
        assertTrue(failure("--data", "clone", "extra").getMessage().contains("unexpected argument extra"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"--data", "--out", "--contact"})
    void optionWithoutAValueIsAUsageError(String option) {
        assertTrue(failure(option).getMessage().contains("option " + option + " needs a value"));
        assertTrue(failure(option, "--public").getMessage().contains("option " + option + " needs a value"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"--data", "--out", "--contact"})
    void repeatedOptionIsAUsageError(String option) {
        assertTrue(failure(option, "a", option, "b").getMessage().contains("option " + option + " is given twice"));
    }

    @Test
    void repeatedPublicIsAUsageError() {
        assertTrue(failure("--data", "clone", "--contact", "team@example.org", "--public", "--public").getMessage().contains("given twice"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", " team@example.org"})
    void blankContactIsAUsageError(String contact) {
        assertTrue(failure("--data", "clone", "--contact", contact).getMessage().contains("invalid --contact"));
    }
}
