package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestFullViewDTO;
import lombok.AllArgsConstructor;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
public class CustomPullRequestViewRepository {

    private final EntityManager entityManager;

    public static final String ACTIVE_PULL_REQUEST_SQL_FILTERS = "pr.is_draft is false and pr.creation_date < " +
            ":endDate and (" +
            "(pr.merge_date is not  null and pr.merge_date >= :startDate) or (pr.merge_date is null and pr.close_date" +
            " is null)" +
            ")";

    public static final String ACTIVE_PULL_REQUEST_SQL_FILTERS_STRING = ACTIVE_PULL_REQUEST_SQL_FILTERS
            .replace(":endDate", "':endDate'")
            .replace(":startDate", "':startDate'");


    private static final String SELECT_PAGINATED_AND_ORDERED = "select pr.id," +
            "       pr.deleted_line_number," +
            "       pr.added_line_number," +
            "       pr.creation_date," +
            "       pr.merge_date," +
            "       pr.close_date," +
            "       pr.state," +
            "       pr.vcs_url," +
            "       pr.head," +
            "       pr.base," +
            "       pr.title," +
            "       pr.commit_number," +
            "       pr.vcs_repository," +
            "       pr.author_login," +
            "       pr.merge_commit_sha" +
            " from exposition_storage.pull_request pr" +
            " where (" +
            ACTIVE_PULL_REQUEST_SQL_FILTERS_STRING +
            "      )" +
            "    and pr.vcs_repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = ':teamId')" +
            " order by pr.:sortingParameter :sortingDirection" +
            " limit :endRange offset :startRange";

    public List<PullRequestFullViewDTO> findAllByTeamIdAndStartEndAndEndDateAndPagination(final UUID teamId,
                                                                                          final Date startDate,
                                                                                          final Date endDate,
                                                                                          final int start,
                                                                                          final int end,
                                                                                          final String sortingParameter,
                                                                                          final String sortingDirection) {
        final String baseQuery = SELECT_PAGINATED_AND_ORDERED
                .replace(":sortingParameter", sortingParameter)
                .replace(":sortingDirection", sortingDirection)
                .replace(":teamId", teamId.toString())
                .replace(":endRange", Integer.toString(end))
                .replace(":startRange", Integer.toString(start))
                .replace(":startDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate))
                .replace(":endDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate));
        final List<PullRequestFullViewDTO> resultList = entityManager.createNativeQuery(baseQuery,
                        PullRequestFullViewDTO.class)
                .getResultList();
        return resultList;
    }

    private static final String FIND_ALL_BY_TEAM_ID_UNTIL_DATE_PAGINATED_AND_SORTED =
            "select pull_request.*, " +
                "c.creation_date ccd, " +
                "prtc.sha        prtcs, " +
                "c.id            ci " +
            "from (select pr.id  pi, " +
                "pr.creation_date    pcd, " +
                "pr.merge_date       pmd, " +
                "pr.state            ps, " +
                "pr.vcs_url          pvu, " +
                "pr.merge_commit_sha pmcs, " +
                "pr.head             ph, " +
                "pr.base             pb, " +
                "pr.author_login, " +
                "pr.vcs_repository, " +
                "pr.title " +
            "from exposition_storage.pull_request pr " +
            "where pr.state in ('merge', 'open') " +
            "and pr.creation_date < ':endDate' " +
            "and pr.vcs_repository_id in (select ttr.repository_id " +
                "from exposition_storage.team_to_repository ttr " +
                "where ttr.team_id = ':teamId') " +
                "order by pr.:sortingParameter :sortingDirection " +
                "limit :endRange offset :startRange) pull_request " +
                "left join exposition_storage.comment c on pull_request.pi = c.pull_request_id " +
                "left join exposition_storage.pull_request_to_commit prtc on prtc.pull_request_id = pull_request.pi ";

    public List<PullRequestView> findAllPullRequestViewByTeamIdUntilEndDatePaginatedAndSorted(final UUID teamId,
                                                                                              final Date startDate,
                                                                                              final Date endDate,
                                                                                              final int start,
                                                                                              final int end,
                                                                                              final String sortingParameter,
                                                                                              final String sortingDirection) {
        final String query = FIND_ALL_BY_TEAM_ID_UNTIL_DATE_PAGINATED_AND_SORTED
                .replace(":sortingParameter", sortingParameter)
                .replace(":sortingDirection", sortingDirection)
                .replace(":teamId", teamId.toString())
                .replace(":endRange", Integer.toString(end))
                .replace(":startRange", Integer.toString(start))
                .replace(":endDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate));
        final List<Object[]> resultList = entityManager.createNativeQuery(query)
                .getResultList();

        final List<PullRequestView> pullRequestViews = new ArrayList<>();
        PullRequestView currentPullRequestView = null;
        final Set<Comment> currentComments = new HashSet<>();
        final Set<String> currentCommitShas = new HashSet<>();
        for (Object[] objects : resultList) {
            final String pullRequestId = (String) objects[0];
            final String commentId = (String) objects[13];
            final Date commentCreationDate = (Timestamp) objects[11];
            final String pullRequestCommitSha = (String) objects[12];
            if (isNull(currentPullRequestView)) {
                currentPullRequestView = mapPaginatedAndSortedResultsToPullRequestView(objects, pullRequestId);
                mapPaginatedAndSortedResultsToCommentAndCommitSha(currentComments, currentCommitShas, commentId, commentCreationDate,
                        pullRequestCommitSha);
            } else if (!currentPullRequestView.getId().equals(pullRequestId)) {
                pullRequestViews.add(currentPullRequestView.toBuilder()
                        .comments(currentComments.stream().toList())
                        .commitShaList(currentCommitShas.stream().toList())
                        .build());

                currentComments.clear();
                currentCommitShas.clear();
                currentPullRequestView = mapPaginatedAndSortedResultsToPullRequestView(objects, pullRequestId);
                mapPaginatedAndSortedResultsToCommentAndCommitSha(currentComments, currentCommitShas, commentId, commentCreationDate,
                        pullRequestCommitSha);
            } else {
                mapPaginatedAndSortedResultsToCommentAndCommitSha(currentComments, currentCommitShas, commentId, commentCreationDate,
                        pullRequestCommitSha);
            }
        }
        if (nonNull(currentPullRequestView)) {
            pullRequestViews.add(currentPullRequestView.toBuilder()
                    .comments(currentComments.stream().toList())
                    .commitShaList(currentCommitShas.stream().toList())
                    .build());
        }
        return pullRequestViews;
    }

    private static void mapPaginatedAndSortedResultsToCommentAndCommitSha(Set<Comment> currentComments, Set<String> currentCommitShas,
                                                                          String commentId,
                                                                          Date commentCreationDate, String pullRequestCommitSha) {
        if (nonNull(commentId) && nonNull(commentCreationDate)) {
            currentComments.add(Comment.builder()
                    .creationDate(commentCreationDate)
                    .id(commentId)
                    .build());
        }
        if (nonNull(pullRequestCommitSha)) {
            currentCommitShas.add(pullRequestCommitSha);
        }
    }

    private static PullRequestView mapPaginatedAndSortedResultsToPullRequestView(Object[] objects, String pullRequestId) {
        final Date pullRequestCreationDate = (Timestamp) objects[1];
        final Date pullRequestMergeDate = (Timestamp) objects[2];
        final String pullRequestState = (String) objects[3];
        final String pullRequestVcsUrl = (String) objects[4];
        final String pullRequestMergeCommitSha = (String) objects[5];
        final String pullRequestHead = (String) objects[6];
        final String pullRequestBase = (String) objects[7];
        final String pullRequestAuthor = (String) objects[8];
        final String pullRequestVcsRepository = (String) objects[9];
        final String pullRequestTitle = (String) objects[10];
        return PullRequestView.builder()
                .id(pullRequestId)
                .creationDate(pullRequestCreationDate)
                .mergeDate(pullRequestMergeDate)
                .status(pullRequestState)
                .vcsUrl(pullRequestVcsUrl)
                .mergeCommitSha(pullRequestMergeCommitSha)
                .head(pullRequestHead)
                .base(pullRequestBase)
                .authorLogin(pullRequestAuthor)
                .repository(pullRequestVcsRepository)
                .title(pullRequestTitle)
                .build();
    }

}
