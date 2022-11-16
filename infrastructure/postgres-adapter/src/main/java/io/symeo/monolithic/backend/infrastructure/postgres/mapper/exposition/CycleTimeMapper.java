package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CycleTimeEntity;
import io.symeo.monolithic.backend.job.domain.model.vcs.CycleTime;

import static java.util.Objects.isNull;

public interface CycleTimeMapper {
    static CycleTimeEntity domainToEntity(CycleTime cycleTime) {
        return CycleTimeEntity.builder()
                .id(cycleTime.getPullRequest().getId())
                .value(cycleTime.getValue())
                .codingTime(cycleTime.getCodingTime())
                .reviewTime(cycleTime.getReviewTime())
                .timeToDeploy(cycleTime.getTimeToDeploy())
                .deployDate(cycleTime.getDeployDate())
                .pullRequestId(cycleTime.getPullRequest().getId())
                .pullRequestAuthorLogin(cycleTime.getPullRequest().getAuthorLogin())
                .pullRequestState(cycleTime.getPullRequest().getStatus())
                .pullRequestVcsRepositoryId(cycleTime.getPullRequest().getRepositoryId())
                .pullRequestVcsRepository(cycleTime.getPullRequest().getRepository())
                .pullRequestVcsUrl(cycleTime.getPullRequest().getVcsUrl())
                .pullRequestTitle(cycleTime.getPullRequest().getTitle())
                .pullRequestCreationDate(cycleTime.getPullRequest().getCreationDate())
                .pullRequestMergeDate(cycleTime.getPullRequest().getMergeDate())
                .pullRequestHead(cycleTime.getPullRequest().getHead())
                .pullRequestUpdateDate(cycleTime.getPullRequest().getLastUpdateDate())
                .build();
    }
}
