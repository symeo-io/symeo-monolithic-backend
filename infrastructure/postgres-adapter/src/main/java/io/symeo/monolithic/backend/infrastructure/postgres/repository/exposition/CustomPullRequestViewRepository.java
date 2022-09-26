package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestFullViewDTO;
import lombok.AllArgsConstructor;

import javax.persistence.EntityManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

}
