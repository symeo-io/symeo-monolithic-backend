package io.symeo.monolithic.backend.job.domain.testing;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CoverageData;

import java.util.Base64;

public class CoverageReportAdapter {
    private static final IstanbulCoverageReportAdapter istanbulCoverageReportAdapter = new IstanbulCoverageReportAdapter();
    private static final JacocoCoverageReportAdapter jacocoCoverageReportAdapter = new JacocoCoverageReportAdapter();

    public static CoverageData extractCoverageFromReport(String base64Report, String reportType) throws SymeoException {
        CoverageReportTypeAdapter reportTypeAdapter = CoverageReportAdapter.getCoverageReportAdapterForType(reportType);

        if (reportTypeAdapter == null) {
            return null;
        }

        String report = new String(Base64.getDecoder().decode(base64Report));

        return reportTypeAdapter.extractCoverageFromReport(report);
    }

    private static CoverageReportTypeAdapter getCoverageReportAdapterForType(String reportType) {
        return switch (reportType) {
            case "istanbul" -> CoverageReportAdapter.istanbulCoverageReportAdapter;
            case "jacoco" -> CoverageReportAdapter.jacocoCoverageReportAdapter;
            default -> null;
        };
    }
}
