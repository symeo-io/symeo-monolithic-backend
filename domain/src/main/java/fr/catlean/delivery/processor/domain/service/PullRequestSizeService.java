package fr.catlean.delivery.processor.domain.service;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.model.account.VcsTeam;
import fr.catlean.delivery.processor.domain.model.insight.DataCompareToLimit;
import fr.catlean.delivery.processor.domain.model.insight.PullRequestHistogram;
import fr.catlean.delivery.processor.domain.port.out.ExpositionStorage;
import lombok.AllArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static fr.catlean.delivery.processor.domain.helper.DateHelper.getWeekStartDateForTheLastWeekNumber;
import static java.util.Objects.isNull;

@AllArgsConstructor
public class PullRequestSizeService {

    private final ExpositionStorage expositionStorage;
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    public void computeAndSavePullRequestSizeHistogram(List<PullRequest> pullRequests,
                                                       OrganisationAccount organisationAccount) {
        final List<PullRequestHistogram> pullRequestHistograms = new ArrayList<>();

        for (VcsTeam vcsTeam : organisationAccount.getVcsConfiguration().getVcsTeams()) {

            final PullRequestHistogram pullRequestHistogram = getPullRequestHistogramForVcsTeam(pullRequests,
                    organisationAccount, vcsTeam);
            pullRequestHistograms.add(pullRequestHistogram);

        }
        expositionStorage.savePullRequestHistograms(pullRequestHistograms);
    }

    private PullRequestHistogram getPullRequestHistogramForVcsTeam(List<PullRequest> pullRequests,
                                                                   OrganisationAccount organisationAccount,
                                                                   VcsTeam vcsTeam) {
        final int pullRequestLineNumberLimit = vcsTeam.getPullRequestLineNumberLimit();
        pullRequests =
                pullRequests.stream()
                        .filter(pullRequest -> vcsTeam.getVcsRepositoryNames().stream()
                                .anyMatch(repositoryName -> pullRequest.getRepository().equals(repositoryName))
                        ).toList();
        final List<DataCompareToLimit> dataCompareToLimits = new ArrayList<>();
        for (Date weekStartDate : getWeekStartDateForTheLastWeekNumber(3 * 4,
                organisationAccount.getTimeZone())) {
            final String weekStart = SDF.format(weekStartDate);
            DataCompareToLimit dataCompareToLimit =
                    DataCompareToLimit.builder().dateAsString(weekStart).build();

            for (PullRequest pullRequest : pullRequests) {
                if (pullRequestIsConsideredAsOpenDuringWeek(pullRequest, weekStartDate)) {
                    if (pullRequest.getAddedLineNumber() + pullRequest.getDeletedLineNumber() >= pullRequestLineNumberLimit) {
                        dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
                    } else {
                        dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();
                    }
                }
            }
            dataCompareToLimits.add(dataCompareToLimit);
        }
        return PullRequestHistogram.builder()
                .type(PullRequestHistogram.SIZE_LIMIT)
                .organisationAccount(organisationAccount.getName())
                .dataByWeek(dataCompareToLimits)
                .limit(organisationAccount.getVcsConfiguration().getVcsTeams().get(0).getPullRequestLineNumberLimit())
                .build();
    }

    private boolean pullRequestIsConsideredAsOpenDuringWeek(final PullRequest pullRequest, final Date weekStartDate) {
        final Date creationDate = pullRequest.getCreationDate();
        final Date mergeDate = pullRequest.getMergeDate();
        if (creationDate.before(weekStartDate)) {
            if (isNull(mergeDate)) {
                return true;
            }
        }
        return false;
    }
}
