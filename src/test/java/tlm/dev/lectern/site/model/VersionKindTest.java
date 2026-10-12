package tlm.dev.lectern.site.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
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
}
