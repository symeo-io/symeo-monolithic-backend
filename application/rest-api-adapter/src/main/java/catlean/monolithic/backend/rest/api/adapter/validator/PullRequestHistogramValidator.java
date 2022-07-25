package catlean.monolithic.backend.rest.api.adapter.validator;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;

import java.util.List;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.INVALID_HISTOGRAM_TYPE;
import static fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram.SIZE_LIMIT;
import static fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram.TIME_LIMIT;

public interface PullRequestHistogramValidator {
    static void validate(String histogramType) throws CatleanException {
        if (!List.of(SIZE_LIMIT, TIME_LIMIT).contains(histogramType)) {
            throw CatleanException.builder()
                    .code(INVALID_HISTOGRAM_TYPE)
                    .message("Invalid histogram type")
                    .build();
        }
    }
}
