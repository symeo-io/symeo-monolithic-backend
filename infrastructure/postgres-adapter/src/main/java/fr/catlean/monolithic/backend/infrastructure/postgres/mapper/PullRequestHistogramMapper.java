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

    private static PullRequestHistogramDataEntity domainToEntity(final DataCompareToLimit dataCompareToLimit,
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

    static PullRequestHistogram entitiesToDomain(final List<PullRequestHistogramDataEntity> histogramDataEntities) {
        final PullRequestHistogramDataEntity firstData = histogramDataEntities.get(0);
        return PullRequestHistogram.builder()
                .organizationAccount(firstData.getId().getOrganizationName())
                .team(firstData.getId().getTeamName())
                .type(firstData.getId().getHistogramType())
                .dataByWeek(histogramDataEntities.stream().map(PullRequestHistogramMapper::entityToDomain).toList())
                .build();
    }

    private static DataCompareToLimit entityToDomain(final PullRequestHistogramDataEntity pullRequestHistogramDataEntity) {
        return DataCompareToLimit.builder()
                .numberBelowLimit(pullRequestHistogramDataEntity.getDataBelowLimit())
                .numberAboveLimit(pullRequestHistogramDataEntity.getDataAboveLimit())
                .dateAsString(pullRequestHistogramDataEntity.getId().getStartDateRange())
                .build();
    }
}
