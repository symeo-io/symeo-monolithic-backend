package io.symeo.monolithic.backend.job.domain.testing;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CoverageData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class IstanbulCoverageReportAdapterTest {
    @Test
    void should_extract_coverage_from_istanbul_report() throws SymeoException, IOException {
        final IstanbulCoverageReportAdapter istanbulCoverageReportAdapter = new IstanbulCoverageReportAdapter();
        final String reportString = Files.readString(Paths.get("target/test-classes/testing/istanbul-report.xml"));

        CoverageData coverage = istanbulCoverageReportAdapter.extractCoverageFromReport(reportString);

        assertThat(coverage).isNotNull();
        assertThat(coverage.getCoveredBranches()).isEqualTo(202);
        assertThat(coverage.getTotalBranchCount()).isEqualTo(580);
    }
}
