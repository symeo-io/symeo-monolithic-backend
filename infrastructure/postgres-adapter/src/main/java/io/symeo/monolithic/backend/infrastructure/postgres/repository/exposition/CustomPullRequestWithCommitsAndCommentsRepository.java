package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import lombok.AllArgsConstructor;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
public class CustomPullRequestWithCommitsAndCommentsRepository {

    private final EntityManager entityManager;

    private static final String FIND_ALL_BY_TEAM_ID_UNTIL_END_DATE_QUERY =
            "select pr.id pi, " +
                    "       pr.creation_date pcd, " +
                    "       pr.merge_date pmd, " +
                    "       pr.state ps, " +
                    "       pr.vcs_url pvu, " +
                    "       pr.merge_commit_sha pmcs, " +
                    "       pr.head ph, " +
                    "       pr.base pb, " +
                    "       c.id ci, " +
                    "       c.creation_date ccd, " +
                    "       prtc.sha  prtcs" +
                    " from exposition_storage.pull_request pr " +
                    "      left join exposition_storage.comment c on pr.id = c.pull_request_id " +
                    "      left join exposition_storage.pull_request_to_commit prtc on prtc.pull_request_id = pr.id " +
                    " where pr.state in ('merge', 'open') " +
                    "  and pr.creation_date < ':endDate' " +
                    "  and pr.vcs_repository_id in (select ttr.repository_id " +
                    "                               from exposition_storage.team_to_repository ttr " +
                    "                               where ttr.team_id = ':teamId') " +
                    " order by pr.id";

    public List<PullRequestView> findAllByTeamIdUntilEndDate(final UUID teamId, final Date endDate) {
        final String query = FIND_ALL_BY_TEAM_ID_UNTIL_END_DATE_QUERY
                .replace(":teamId", teamId.toString())
                .replace(":endDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate));
        final List<Object[]> resultList = entityManager.createNativeQuery(query)
                .getResultList();

        final List<PullRequestView> pullRequestViews = new ArrayList<>();
        PullRequestView currentPullRequestView = null;
        final Set<Comment> currentComments = new HashSet<>();
        final Set<String> currentCommitShas = new HashSet<>();
        for (Object[] objects : resultList) {
            final String pullRequestId = (String) objects[0];
            final String commentId = (String) objects[8];
            final Date commentCreationDate = (Timestamp) objects[9];
            final String pullRequestCommitSha = (String) objects[10];
            if (isNull(currentPullRequestView)) {
                currentPullRequestView = mapResultsToPullRequestView(objects, pullRequestId);
                mapResultsToCommentAndCommitSha(currentComments, currentCommitShas, commentId, commentCreationDate,
                        pullRequestCommitSha);
            } else if (!currentPullRequestView.getId().equals(pullRequestId)) {
                pullRequestViews.add(currentPullRequestView.toBuilder()
                        .comments(currentComments.stream().toList())
                        .commitShaList(currentCommitShas.stream().toList())
                        .build());

                currentComments.clear();
                currentCommitShas.clear();
                currentPullRequestView = mapResultsToPullRequestView(objects, pullRequestId);
                mapResultsToCommentAndCommitSha(currentComments, currentCommitShas, commentId, commentCreationDate,
                        pullRequestCommitSha);
            } else {
                mapResultsToCommentAndCommitSha(currentComments, currentCommitShas, commentId, commentCreationDate,
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

    private static void mapResultsToCommentAndCommitSha(Set<Comment> currentComments, Set<String> currentCommitShas,
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

    private static PullRequestView mapResultsToPullRequestView(Object[] objects, String pullRequestId) {
        final Date pullRequestCreationDate = (Timestamp) objects[1];
        final Date pullRequestMergeDate = (Timestamp) objects[2];
        final String pullRequestState = (String) objects[3];
        final String pullRequestVcsUrl = (String) objects[4];
        final String pullRequestMergeCommitSha = (String) objects[5];
        final String pullRequestHead = (String) objects[6];
        final String pullRequestBase = (String) objects[7];
        return PullRequestView.builder()
                .id(pullRequestId)
                .creationDate(pullRequestCreationDate)
                .mergeDate(pullRequestMergeDate)
                .status(pullRequestState)
                .vcsUrl(pullRequestVcsUrl)
                .mergeCommitSha(pullRequestMergeCommitSha)
                .head(pullRequestHead)
                .base(pullRequestBase)
                .build();
    }
}
