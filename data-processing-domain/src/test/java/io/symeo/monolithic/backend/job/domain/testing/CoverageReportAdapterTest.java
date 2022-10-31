package io.symeo.monolithic.backend.job.domain.testing;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class CoverageReportAdapterTest {
    @Test
    void should_extract_coverage_from_base_64_report() throws SymeoException, IOException {
        final String reportString = Files.readString(Paths.get("target/test-classes/testing/istanbul-report.xml"));
        final String base64Report = Base64.getEncoder().encodeToString(reportString.getBytes());

        Float coverage = CoverageReportAdapter.extractCoverageFromReport(base64Report, "istanbul");

        assertThat(coverage).isEqualTo((float) 0.34827587);
    }
}