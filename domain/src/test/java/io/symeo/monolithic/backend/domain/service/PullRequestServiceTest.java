package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.domain.model.core.Page;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.service.platform.vcs.PullRequestService;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PullRequestServiceTest {

    private final static Faker faker = new Faker();

    @Test
    void should_return_pull_request_views_page_given_a_team_id_and_range_date_and_pagination() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final PullRequestService pullRequestService = new PullRequestService(expositionStorageAdapter);
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate(
                "2022-01-01");
        final Date endDate = stringToDate("2022-06-01");
        final int pageIndex = 0;
        final int pageSize = 10;
        final int count = 25;
        final String sortingParameter = "creation_date";
        final String sortingDirection = "asc";
        final List<PullRequestView> pullRequestViews =
                List.of(PullRequestView.builder().id(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.ancient().god()).build());

        // When
        when(expositionStorageAdapter.readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(teamId,
                startDate, endDate, pageIndex, pageSize, sortingParameter,sortingDirection))
                .thenReturn(pullRequestViews);
        when(expositionStorageAdapter.countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(teamId,
                startDate, endDate)).thenReturn(count);
        final Page<PullRequestView> pullRequestViewPage =
                pullRequestService.getPullRequestViewsPageForTeamIdAndStartDateAndEndDateAndPaginationSorted(teamId,
                        startDate,
                        endDate, pageIndex, pageSize, sortingParameter, sortingDirection);

        // Then
        assertThat(pullRequestViewPage.getContent()).isEqualTo(pullRequestViews);
        assertThat(pullRequestViewPage.getTotalPageNumber()).isEqualTo(3);
    }

    @Test
    void should_raise_an_exception_for_invalid_sorting_parameter() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final PullRequestService pullRequestService = new PullRequestService(expositionStorageAdapter);
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate(
                "2022-01-01");
        final Date endDate = stringToDate("2022-06-01");
        final int pageIndex = 0;
        final int pageSize = 10;
        final int count = 25;
        final String sortingParameter = faker.ancient().god();
        final String sortingDirection = "asc";

        // When
        SymeoException symeoException = null;
        try {
            pullRequestService.getPullRequestViewsPageForTeamIdAndStartDateAndEndDateAndPaginationSorted(teamId,
                    startDate,
                    endDate, pageIndex, pageSize, sortingParameter, sortingDirection);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(SymeoExceptionCode.INVALID_SORTING_PARAMETER);
    }

    @Test
    void should_raise_an_exception_for_invalid_sorting_direction_parameter() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final PullRequestService pullRequestService = new PullRequestService(expositionStorageAdapter);
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate(
                "2022-01-01");
        final Date endDate = stringToDate("2022-06-01");
        final int pageIndex = 0;
        final int pageSize = 10;
        final int count = 25;
        final String sortingParameter = "creation_date";
        final String sortingDirection = faker.ancient().primordial();

        // When
        SymeoException symeoException = null;
        try {
            pullRequestService.getPullRequestViewsPageForTeamIdAndStartDateAndEndDateAndPaginationSorted(teamId,
                    startDate,
                    endDate, pageIndex, pageSize, sortingParameter, sortingDirection);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(SymeoExceptionCode.INVALID_SORTING_DIRECTION);
    }


}
