package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiece;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import lombok.AllArgsConstructor;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class CustomCycleTimeRepository {

    private final EntityManager entityManager;

    private static final String FIND_ALL_BY_TEAM_ID_BETWEEN_START_DATE_AND_END_DATE =
            "select ct.id, " +
                    "       ct.value, " +
                    "       ct.coding_time, " +
                    "       ct.review_time, " +
                    "       ct.time_to_deploy, " +
                    "       ct.deploy_date, " +
                    "       ct.pull_request_id, " +
                    "       ct.pull_request_author_login, " +
                    "       ct.pull_request_state, " +
                    "       ct.pull_request_vcs_repository_id, " +
                    "       ct.pull_request_vcs_repository, " +
                    "       ct.pull_request_vcs_url, " +
                    "       ct.pull_request_title, " +
                    "       ct.pull_request_creation_date, " +
                    "       ct.pull_request_merge_date, " +
                    "       ct.pull_request_head " +

                    " from exposition_storage.cycle_time ct " +
                    " where ct.deploy_date > ':startDate' and ct.deploy_date <= ':endDate' " +
                    " and ct.pull_request_vcs_repository_id in (select ttr.repository_id " +
                    "                               from exposition_storage.team_to_repository ttr " +
                    "                               where ttr.team_id = ':teamId') " +
                    " order by ct.id";

    public List<CycleTime> findAllCycleTimeByTeamIdBetweenStartDateAndEndDate(UUID teamId, Date startDate, Date endDate) {
        final String query = FIND_ALL_BY_TEAM_ID_BETWEEN_START_DATE_AND_END_DATE
                .replace(":teamId", teamId.toString())
                .replace(":startDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate))
                .replace(":endDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate));
        final List<Object[]> resultList = entityManager.createNativeQuery(query).getResultList();

        final List<CycleTime> cycleTimes = new ArrayList<>();
        for (Object[] object : resultList) {
            PullRequestView pullRequestView = mapResultToPullRequestView(object);
            CycleTime cycleTime = mapResultToCycleTime(object, pullRequestView);
            cycleTimes.add(cycleTime);
        }
        return cycleTimes;
    }

    private CycleTime mapResultToCycleTime(Object[] object, PullRequestView pullRequestView) {
        final String cycleTimeId = (String) object[0];
        final Long cycleTimeValue = isNull(object[1]) ? null : ((BigInteger) object[1]).longValue();
        final Long codingTime = isNull(object[2]) ? null : ((BigInteger) object[2]).longValue();
        final Long reviewTime = isNull(object[3]) ? null : ((BigInteger) object[3]).longValue();
        final Long timeToDeploy = isNull(object[4]) ? null : ((BigInteger) object[4]).longValue();
        final Date deployDate = (Timestamp) object[5];
        return CycleTime.builder()
                .id(cycleTimeId)
                .value(cycleTimeValue)
                .codingTime(codingTime)
                .reviewTime(reviewTime)
                .timeToDeploy(timeToDeploy)
                .deployDate(deployDate)
                .pullRequestView(pullRequestView)
                .build();
    }

    private PullRequestView mapResultToPullRequestView(Object[] object) {
        final String authorLogin = (String) object[7];
        final String status = (String) object[8];
        final String vcs_repository = (String) object[10];
        final String vcs_url = (String) object[11];
        final String title = (String) object[12];
        final Date creationDate = (Timestamp) object[13];
        final Date mergeDate = (Timestamp) object[14];
        final String head = (String) object[15];
        return PullRequestView.builder()
                .authorLogin(authorLogin)
                .status(status)
                .repository(vcs_repository)
                .vcsUrl(vcs_url)
                .title(title)
                .creationDate(creationDate)
                .mergeDate(mergeDate)
                .head(head)
                .build();
    }

    public List<CycleTimePiece> findAllCycleTimePiecesForTeamIdBetweenStartDateAndEndDate(UUID teamId, Date startDate, Date endDate) {
        final String query = FIND_ALL_BY_TEAM_ID_BETWEEN_START_DATE_AND_END_DATE
                .replace(":teamId", teamId.toString())
                .replace(":startDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate))
                .replace(":endDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate));

        final List<Object[]> resultList = entityManager.createNativeQuery(query).getResultList();
        final List<CycleTimePiece> cycleTimePieces = new ArrayList<>();
        CycleTimePiece cycleTimePiece = new CycleTimePiece();
        for (Object[] object : resultList) {
            cycleTimePiece = mapResultToCyleTimePiece(object);
            cycleTimePieces.add(cycleTimePiece);
        }
        return cycleTimePieces;
    }

    private static final String FIND_ALL_BY_TEAM_ID_BETWEEN_START_DATE_AND_END_DATE_PAGINATED_AND_SORTED =
            "select ct.id, " +
                    "       ct.value, " +
                    "       ct.coding_time, " +
                    "       ct.review_time, " +
                    "       ct.time_to_deploy, " +
                    "       ct.deploy_date, " +
                    "       ct.pull_request_id, " +
                    "       ct.pull_request_author_login, " +
                    "       ct.pull_request_state, " +
                    "       ct.pull_request_vcs_repository_id, " +
                    "       ct.pull_request_vcs_repository, " +
                    "       ct.pull_request_vcs_url, " +
                    "       ct.pull_request_title, " +
                    "       ct.pull_request_creation_date, " +
                    "       ct.pull_request_merge_date, " +
                    "       ct.pull_request_head " +

                    " from exposition_storage.cycle_time ct " +
                    " where ct.deploy_date > ':startDate' and ct.deploy_date <= ':endDate' " +
                    " and ct.pull_request_vcs_repository_id in (select ttr.repository_id " +
                    "                               from exposition_storage.team_to_repository ttr " +
                    "                               where ttr.team_id = ':teamId') " +

                    "order by ct.:sortingParameter :sortingDirection " +
                    "limit :endRange offset :startRange";

    public List<CycleTimePiece> findAllCycleTimePiecesForTeamIdBetweenStartDateAndEndDatePaginatedAndSorted(UUID teamId,
                                                                                                            Date startDate,
                                                                                                            Date endDate,
                                                                                                            int start,
                                                                                                            int end,
                                                                                                            String sortingDatabaseParameter,
                                                                                                            String sortingDatabaseDirection) {
        final String query = FIND_ALL_BY_TEAM_ID_BETWEEN_START_DATE_AND_END_DATE_PAGINATED_AND_SORTED
                .replace(":teamId", teamId.toString())
                .replace(":startDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate))
                .replace(":endDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate))
                .replace(":sortingParameter", mapSortingParameter(sortingDatabaseParameter))
                .replace(":sortingDirection", sortingDatabaseDirection)
                .replace(":endRange", Integer.toString(end))
                .replace(":startRange", Integer.toString(start));

        final List<Object[]> resultList = entityManager.createNativeQuery(query).getResultList();
        final List<CycleTimePiece> cycleTimePieces = new ArrayList<>();
        CycleTimePiece cycleTimePiece = new CycleTimePiece();
        for (Object[] object : resultList) {
            cycleTimePiece = mapResultToCyleTimePiece(object);
            cycleTimePieces.add(cycleTimePiece);
        }
        return cycleTimePieces;
    }

    private CycleTimePiece mapResultToCyleTimePiece(Object[] object) {
        final String cycleTimeId = (String) object[0];
        final Long cycleTimeValue = isNull(object[1]) ? null : ((BigInteger) object[1]).longValue();
        final Long codingTime = isNull(object[2]) ? null : ((BigInteger) object[2]).longValue();
        final Long reviewTime = isNull(object[3]) ? null : ((BigInteger) object[3]).longValue();
        final Long timeToDeploy = isNull(object[4]) ? null : ((BigInteger) object[4]).longValue();
        final Date deployDate = (Timestamp) object[5];
        final String pullRequestId = (String) object[6];
        final String authorLogin = (String) object[7];
        final String state = (String) object[8];
        final String vcsRepositoryId = (String) object[9];
        final String vcsRepository = (String) object[10];
        final String vcsUrl = (String) object[11];
        final String title = (String) object[12];
        final Date creationDate = (Timestamp) object[13];
        final Date mergeDate = (Timestamp) object[14];
        final String head = (String) object[15];

        return CycleTimePiece.builder()
                .id(pullRequestId)
                .creationDate(creationDate)
                .mergeDate(mergeDate)
                .state(state)
                .vcsUrl(vcsUrl)
                .title(title)
                .author(authorLogin)
                .repository(vcsRepository)
                .cycleTime(cycleTimeValue)
                .codingTime(codingTime)
                .reviewTime(reviewTime)
                .timeToDeploy(timeToDeploy)
                .build();
    }

    private static final Map<String, String> MAP_SORTING_PARAMETER_TO_ALIAS = Map.of(
            "creation_date", "pull_request_creation_date",
            "state", "pull_request_state",
            "merge_date", "pull_request_merge_date",
            "id", "pull_request_id",
            "vcs_url", "pull_request_vcs_url",
            "title", "pull_request_title",
            "author_login", "pull_request_author_login",
            "vcs_repository", "pull_request_vcs_repository"
    );

    private String mapSortingParameter(String sortingDatabaseParameter) {
        return MAP_SORTING_PARAMETER_TO_ALIAS.getOrDefault(sortingDatabaseParameter, sortingDatabaseParameter);
    }
}
