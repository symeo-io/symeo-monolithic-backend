package io.symeo.monolithic.backend.job.domain.testing;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

public interface CoverageReportTypeAdapter {
    public Float extractCoverageFromReport(String report) throws SymeoException;
}
