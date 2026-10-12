package tlm.dev.lectern.site.model;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

public record GameInfo(OptionalLong worldVersion, OptionalLong protocolVersion, Optional<PackVersion> packVersion) {

    public GameInfo {
        Objects.requireNonNull(worldVersion, "worldVersion");
        Objects.requireNonNull(protocolVersion, "protocolVersion");
        Objects.requireNonNull(packVersion, "packVersion");
    }
}
