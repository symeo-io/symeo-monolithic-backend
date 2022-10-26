package io.symeo.monolithic.backend.domain.bff.model.metric;

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
