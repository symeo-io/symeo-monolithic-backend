package io.symeo.monolithic.backend.job.domain.testing;

public class IstanbulCoverageReportAdapter implements CoverageReportTypeAdapter {
    @Override
    public Float extractCoverageFromReport(String report) {
        return (float) 0; // TODO implement
    }
}
