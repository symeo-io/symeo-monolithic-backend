package io.symeo.monolithic.backend.domain.bff.model.metric;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Builder
@Value
public class TestingMetrics {
    Boolean hasData;
    Date currentStartDate;
    Date currentEndDate;
    Date previousStartDate;
    Date previousEndDate;
    Float coverage;
    Float coverageTendencyPercentage;
    Integer testCount;
    Float testCountTendencyPercentage;
    Float testToCodeRatio;
    Float testToCodeRatioTendencyPercentage;
    Integer testLineCount;
    Integer codeLineCount;
    Integer unitTestCount;
    Float unitTestCountTendencyPercentage;
    Integer integrationTestCount;
    Float integrationTestCountTendencyPercentage;
    Integer endToEndTestCount;
    Float endToEndTestCountTendencyPercentage;
}
