package io.symeo.monolithic.backend.job.domain.testing;

import java.util.Base64;

public class CoverageReportAdapter {
    private static final IstanbulCoverageReportAdapter istanbulCoverageReportAdapter = new IstanbulCoverageReportAdapter();

    public static Float extractCoverageFromReport(String base64Report, String reportType) {
        CoverageReportTypeAdapter reportTypeAdapter = CoverageReportAdapter.getCoverageReportAdapterForType(reportType);

        if (reportTypeAdapter == null) {
            return null;
        }

        String report = new String(Base64.getDecoder().decode(base64Report));

        return reportTypeAdapter.extractCoverageFromReport(report);
    }

    private static CoverageReportTypeAdapter getCoverageReportAdapterForType(String reportType) {
        switch (reportType) {
            case "istanbul": // TODO add support for other framework
                return CoverageReportAdapter.istanbulCoverageReportAdapter;
            default:
                return null;
        }
    }
}
