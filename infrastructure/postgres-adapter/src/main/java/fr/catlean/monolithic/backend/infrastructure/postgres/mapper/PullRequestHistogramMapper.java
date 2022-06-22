package fr.catlean.monolithic.backend.infrastructure.postgres.mapper;

import fr.catlean.monolithic.backend.domain.model.insight.DataCompareToLimit;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.PullRequestHistogramDataEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.id.HistogramId;

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
                                .organizationName(pullRequestHistogram.getOrganizationAccount())
                                .teamName(pullRequestHistogram.getTeam())
                                .startDateRange(dataCompareToLimit.getDateAsString())
                                .build()
                )
                .dataBelowLimit(dataCompareToLimit.getNumberBelowLimit())
                .dataAboveLimit(dataCompareToLimit.getNumberAboveLimit())
                .build();
    }
}
