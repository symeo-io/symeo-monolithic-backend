package io.symeo.monolithic.backend.job.domain.testing;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CoverageData;

public interface CoverageReportTypeAdapter {
    CoverageData extractCoverageFromReport(String report) throws SymeoException;
}
