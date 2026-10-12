package tlm.dev.lectern.site.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VersionKindTest {

    static Stream<Arguments> kinds() {
        return Stream.of(
                Arguments.of(new VersionKind.Release(), "release", false),
                Arguments.of(new VersionKind.ReleaseCandidate(), "release-candidate", true),
                Arguments.of(new VersionKind.PreRelease(), "pre-release", true),
                Arguments.of(new VersionKind.Snapshot(), "snapshot", true));
    }

    @ParameterizedTest
    @MethodSource("kinds")
    void labelIsTheContractLabel(VersionKind kind, String label, boolean unstable) {
        assertEquals(label, kind.label());
    }

    @ParameterizedTest
    @MethodSource("kinds")
    void everyKindExceptReleaseIsUnstable(VersionKind kind, String label, boolean unstable) {
        assertEquals(unstable, kind.unstable());
    }

    @ParameterizedTest
    @MethodSource("kinds")
    void everyLabelIsReadBack(VersionKind kind, String label, boolean unstable) {
        assertEquals(Optional.of(kind), VersionKind.ofLabel(label));
    }

    @Test
    void unknownLabelIsNotAKind() {
        assertEquals(Optional.empty(), VersionKind.ofLabel("beta"));
        assertEquals(Optional.empty(), VersionKind.ofLabel("Release"));
    }

    @Test
    void allListsTheFourKinds() {
        assertEquals(4, VersionKind.ALL.size());
    }
}
