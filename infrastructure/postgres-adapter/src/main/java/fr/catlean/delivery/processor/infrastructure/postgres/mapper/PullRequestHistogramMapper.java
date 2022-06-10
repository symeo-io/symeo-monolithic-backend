package fr.catlean.delivery.processor.infrastructure.postgres.mapper;

import fr.catlean.delivery.processor.domain.model.insight.DataCompareToLimit;
import fr.catlean.delivery.processor.domain.model.insight.PullRequestHistogram;
import fr.catlean.delivery.processor.infrastructure.postgres.entity.PullRequestHistogramDataEntity;
import fr.catlean.delivery.processor.infrastructure.postgres.entity.id.HistogramId;

import java.util.List;

public interface PullRequestHistogramMapper {
    static List<PullRequestHistogramDataEntity> domainToEntities(final PullRequestHistogram pullRequestHistogram) {
        return pullRequestHistogram.getDataByWeek().stream()
                .map(dataCompareToLimit -> domainToEntity(dataCompareToLimit, pullRequestHistogram))
                .toList();
    }

    static PullRequestHistogramDataEntity domainToEntity(final DataCompareToLimit dataCompareToLimit,
                                                         final PullRequestHistogram pullRequestHistogram) {
        return PullRequestHistogramDataEntity.builder()
                .id(
                        HistogramId.builder()
                                .histogramType(pullRequestHistogram.getType())
                                .organisationName(pullRequestHistogram.getOrganisationAccount())
                                .teamName(pullRequestHistogram.getTeam())
                                .startDateRange(dataCompareToLimit.getDateAsString())
                                .build()
                )
                .dataBelowLimit(dataCompareToLimit.getNumberBelowLimit())
                .dataAboveLimit(dataCompareToLimit.getNumberAboveLimit())
                .build();
    }
}
