package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.core.Page;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.frontend.contract.api.model.PullRequestContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.PullRequestPageContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.PullRequestsResponseContract;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.exceptionToContract;
import static java.util.Objects.nonNull;

public interface PullRequestContractMapper {

    static PullRequestsResponseContract errorToContract(final SymeoException symeoException) {
        final PullRequestsResponseContract pullRequestsResponseContract = new PullRequestsResponseContract();
        pullRequestsResponseContract.setErrors(List.of(exceptionToContract(symeoException)));
        return pullRequestsResponseContract;
    }

    static PullRequestsResponseContract toContract(final Page<PullRequestView> pullRequestViewPage, final ZoneId zoneId,
                                                   final Date endDate) {
        final PullRequestsResponseContract pullRequestsResponseContract = new PullRequestsResponseContract();
        final PullRequestPageContract pullRequestsPage = new PullRequestPageContract();
        pullRequestsPage.setTotalPageNumber(pullRequestViewPage.getTotalPageNumber());
        pullRequestsPage.setTotalItemNumber(pullRequestViewPage.getTotalItemNumber());
        pullRequestsPage.setPullRequests(pullRequestViewPage.getContent().stream()
                .map(pullRequest -> pullRequestToContract(pullRequest, zoneId, endDate)).collect(Collectors.toList()));
        pullRequestsResponseContract.setPullRequestsPage(pullRequestsPage);
        return pullRequestsResponseContract;
    }

    private static PullRequestContract pullRequestToContract(final PullRequestView pullRequestView, final ZoneId zoneId,
                                                             final Date endDate) {
        final PullRequestContract pullRequestContract = new PullRequestContract();
        pullRequestContract.setAuthor(pullRequestView.getAuthorLogin());
        pullRequestContract.setCommitNumber(pullRequestView.getCommitNumber());
        pullRequestContract.setCreationDate(pullRequestView.getCreationDate().toInstant().atZone(zoneId));
        pullRequestContract.setMergeDate(nonNull(pullRequestView.getMergeDate()) ?
                pullRequestView.getCreationDate().toInstant().atZone(zoneId) : null);
        pullRequestContract.setId(pullRequestView.getId());
        pullRequestContract.setStatus(pullRequestView.getStatus());
        pullRequestContract.setSize(BigDecimal.valueOf(pullRequestView.getSize()));
        pullRequestContract.setDaysOpened(BigDecimal.valueOf(pullRequestView.getDaysOpened(endDate)));
        pullRequestContract.setTitle(pullRequestView.getTitle());
        pullRequestContract.setVcsUrl(pullRequestView.getVcsUrl());
        pullRequestContract.setVcsRepository(pullRequestView.getRepository());
        pullRequestContract.setStatus(pullRequestContract.getStatus());
        return pullRequestContract;
    }
}
