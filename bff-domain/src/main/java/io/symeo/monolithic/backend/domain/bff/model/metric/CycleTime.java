package io.symeo.monolithic.backend.domain.bff.model.metric;

import io.symeo.monolithic.backend.domain.bff.model.vcs.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
@Slf4j
public class CycleTime {

    String id;
    Long value;
    Long codingTime;
    Long reviewTime;
    Long timeToDeploy;
    Date deployDate;
    Date updateDate;
    // To display PR data on graphs
    PullRequestView pullRequestView;
    String startDateRange;

    public CycleTime mapDeployDateToClosestRangeDate(List<Date> rangeDates, Date deployDate) {
        String startDateRange;
        if (ChronoUnit.MINUTES.between(deployDate.toInstant(), rangeDates.get(rangeDates.size() - 1).toInstant()) < 0) {
            startDateRange = dateToString(rangeDates.get(rangeDates.size() - 1));
        } else {
            Date closestRangeDate = rangeDates.get(0);
            closestRangeDate = getClosestRangeDate(rangeDates, deployDate, closestRangeDate);
            startDateRange = dateToString(closestRangeDate);
        }
        return this.toBuilder().startDateRange(startDateRange).build();
    }

    private Date getClosestRangeDate(List<Date> rangeDates, Date deployDate, Date closestRangeDate) {
        for (Date rangeDate : rangeDates) {
            if (Math.abs(TimeUnit.DAYS.convert(deployDate.getTime() - rangeDate.getTime(),
                    TimeUnit.MILLISECONDS)) < Math.abs(TimeUnit.DAYS.convert(deployDate.getTime() - closestRangeDate.getTime(),
                    TimeUnit.MILLISECONDS))) {
                closestRangeDate = rangeDate;
            }
        }
        return closestRangeDate;
    }
}
