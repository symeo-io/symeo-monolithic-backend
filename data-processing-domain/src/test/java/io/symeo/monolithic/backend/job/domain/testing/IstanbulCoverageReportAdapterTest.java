package io.symeo.monolithic.backend.job.domain.testing;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
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

        Float coverage = istanbulCoverageReportAdapter.extractCoverageFromReport(reportString);

        assertThat(coverage).isEqualTo((float) 0.34827587);
    }
}
