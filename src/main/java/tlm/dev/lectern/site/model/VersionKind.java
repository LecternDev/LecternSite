package tlm.dev.lectern.site.model;

import java.util.List;
import java.util.Optional;

public sealed interface VersionKind {

    List<VersionKind> ALL = List.of(new Release(), new ReleaseCandidate(), new PreRelease(), new Snapshot());

    String label();

    default boolean unstable() {
        return switch (this) {
            case Release _ -> false;
            case ReleaseCandidate _, PreRelease _, Snapshot _ -> true;
        };
    }

    static Optional<VersionKind> ofLabel(String label) {
        return ALL.stream().filter(kind -> kind.label().equals(label)).findFirst();
    }

    record Release() implements VersionKind {

        @Override
        public String label() {
            return "release";
        }
    }

    record ReleaseCandidate() implements VersionKind {

        @Override
        public String label() {
            return "release-candidate";
        }
    }

    record PreRelease() implements VersionKind {

        @Override
        public String label() {
            return "pre-release";
        }
    }

    record Snapshot() implements VersionKind {

        @Override
        public String label() {
            return "snapshot";
        }
    }
}
