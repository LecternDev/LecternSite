package tlm.dev.lectern.site.model;

public sealed interface VersionKind {

    String label();

    default boolean unstable() {
        return switch (this) {
            case Release _ -> false;
            case ReleaseCandidate _, PreRelease _, Snapshot _ -> true;
        };
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
