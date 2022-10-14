package io.symeo.monolithic.backend.domain.model.testing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestToCodeRatio {
    Integer totalCodeLines;
    Integer testCodeLines;

    public Float getRatio() {
        return (float) testCodeLines / totalCodeLines;
    }
}
