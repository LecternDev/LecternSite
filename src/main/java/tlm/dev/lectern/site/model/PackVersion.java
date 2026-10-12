package tlm.dev.lectern.site.model;

public sealed interface PackVersion {

    record Single(int version) implements PackVersion {
    }

    record Split(int data, int resource) implements PackVersion {
    }

    record Versioned(int dataMajor, int dataMinor, int resourceMajor, int resourceMinor) implements PackVersion {
    }
}
