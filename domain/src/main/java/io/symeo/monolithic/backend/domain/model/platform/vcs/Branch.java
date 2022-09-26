package io.symeo.monolithic.backend.domain.model.platform.vcs;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Branch {
    public static final String ALL = "branches";
    String name;
}
