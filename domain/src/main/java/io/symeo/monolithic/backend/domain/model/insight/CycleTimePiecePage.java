package io.symeo.monolithic.backend.domain.model.insight;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class CycleTimePiecePage {
    int totalNumberOfPieces;
    int totalNumberOfPages;
    List<CycleTimePiece> cycleTimePieces;
}
