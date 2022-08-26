package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import com.github.javafaker.Faker;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.frontend.contract.api.LeadTimeApi;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeResponseContractLeadTime;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricsContract;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@Tags(@Tag(name = "LeadTime"))
@AllArgsConstructor
public class LeadTimeRestApiAdapter implements LeadTimeApi {

    private final static Faker faker = new Faker();

    private static LeadTimeResponseContractLeadTime generateLeadTimeStub() {
        final LeadTimeResponseContractLeadTime leadTimeResponseContractLeadTime =
                new LeadTimeResponseContractLeadTime();
        leadTimeResponseContractLeadTime.setAverage(getMetricsContractStub());
        leadTimeResponseContractLeadTime.setCodingTime(getMetricsContractStub());
        leadTimeResponseContractLeadTime.setReviewLag(getMetricsContractStub());
        leadTimeResponseContractLeadTime.setReviewTime(getMetricsContractStub());
        leadTimeResponseContractLeadTime.setTimeToDeploy(getMetricsContractStub());
        return leadTimeResponseContractLeadTime;
    }

    private static MetricsContract getMetricsContractStub() {
        final MetricsContract average = new MetricsContract();
        average.setValue(new BigDecimal(Float.toString(faker.number().numberBetween(1, 200) / 10f)));
        average.setTendencyPercentage(new BigDecimal(Float.toString(faker.number().numberBetween(-2000, 2000) / 10f)));
        return average;
    }

    @Override
    public ResponseEntity<LeadTimeResponseContract> getLeadTimeMetrics(UUID teamId, String startDate, String endDate) {
        final LeadTimeResponseContract leadTimeResponseContract = new LeadTimeResponseContract();
        leadTimeResponseContract.setLeadTime(generateLeadTimeStub());
        return ResponseEntity.ok(leadTimeResponseContract);
    }
}
