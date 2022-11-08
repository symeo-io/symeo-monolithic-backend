package io.symeo.monolithic.backend.job.domain.testing;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CoverageData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class JacocoCoverageReportAdapterTest {
    @Test
    void should_extract_coverage_from_istanbul_report() throws SymeoException, IOException {
        final JacocoCoverageReportAdapter jacocoCoverageReportAdapter = new JacocoCoverageReportAdapter();
        final String reportString = Files.readString(Paths.get("target/test-classes/testing/jacoco-report.xml"));

        CoverageData coverage = jacocoCoverageReportAdapter.extractCoverageFromReport(reportString);

        assertThat(coverage).isNotNull();
        assertThat(coverage.getCoveredBranches()).isEqualTo(455);
        assertThat(coverage.getTotalBranchCount()).isEqualTo(1880);
    }
}
