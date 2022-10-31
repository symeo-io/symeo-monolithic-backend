package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import lombok.AllArgsConstructor;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class CustomCommitRepository {

    private static final String FIND_COMMITS_BY_TEAM_ID_QUERY = "select c.sha cs, c.author_login cal, c.repository_id" +
            " cri, " +
            " c.message cm, c.date cd, ctps.parent_sha ctpsp" +
            " from exposition_storage.commit c" +
            " left join exposition_storage.commit_to_parent_sha ctps on ctps.sha = c.sha " +
            " where c.repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr " +
            "                                 where ttr.team_id = ':teamId') ";

    private final EntityManager entityManager;

    public List<CommitView> findAllByTeamId(final UUID teamId) {
        final Query nativeQuery = entityManager.createNativeQuery(FIND_COMMITS_BY_TEAM_ID_QUERY.replace(":teamId",
                teamId.toString()));
        final List<Object[]> resultList = nativeQuery.getResultList();
        return mapResultListToCommitList(resultList);
    }

    private static List<CommitView> mapResultListToCommitList(List<Object[]> resultList) {
        final Map<String, CommitView> commitMap = new HashMap<>();
        for (Object[] objects : resultList) {
            final String sha = (String) objects[0];
            final String parentSha = (String) objects[5];
            if (commitMap.containsKey(sha)) {
                commitMap.get(sha).getParentShaList().add(parentSha);
            } else {
                final CommitView commit = buildCommit(objects, sha, parentSha);
                commitMap.put(
                        sha,
                        commit
                );
            }
        }
        return commitMap.values().stream().toList();
    }

    private static CommitView buildCommit(Object[] objects, String sha, String parentSha) {
        final String authorLogin = (String) objects[1];
        final String repositoryId = (String) objects[2];
        final String message = (String) objects[3];
        final Date date = (Timestamp) objects[4];
        return CommitView.builder()
                .author(authorLogin)
                .repositoryId(repositoryId)
                .message(message)
                .date(date)
                .sha(sha)
                .parentShaList(isNull(parentSha) ? new ArrayList<>() :
                        new ArrayList<>(List.of(parentSha)))
                .build();
    }
}
