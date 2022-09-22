package io.symeo.monolithic.backend.domain.model.vcs;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.CommitHistory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;

public class CommitHistoryTest {

    private static final Faker faker = new Faker();

    @Test
    void should_compute_elapsed_time_for_a_commit_to_arrive_to_given_branch() throws SymeoException {
        // Given
        final String matchedBranch = "main";
        final String mergeSha = faker.gameOfThrones().character();
        final Commit mergeCommit = Commit.builder()
                .sha(mergeSha)
                .date(stringToDate("2022-01-01"))
                .head(faker.name().firstName())
                .build();
        final String sha1 = faker.rickAndMorty().character() + "-1";
        final String sha2 = faker.rickAndMorty().character() + "-2";
        final String sha3 = faker.rickAndMorty().character() + "-3";
        final String sha4 = faker.rickAndMorty().character() + "-4";
        final Date mergeDateOnBranch = stringToDate("2022-01-15");
        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(
                List.of(
                        mergeCommit,
                        Commit.builder()
                                .sha(sha1)
                                .parentShaList(List.of(mergeSha, faker.harryPotter().house()))
                                .build(),
                        Commit.builder()
                                .sha(sha2)
                                .parentShaList(List.of(sha1))
                                .build(),
                        Commit.builder()
                                .sha(sha3)
                                .parentShaList(List.of(sha2, faker.name().firstName()))
                                .date(mergeDateOnBranch)
                                .build(),
                        Commit.builder()
                                .sha(sha4)
                                .parentShaList(List.of(sha3))
                                .head(matchedBranch)
                                .build()
                )
        );

        // When
        final Date mergeDateForCommitOnBranch =
                commitHistory.getMergeDateForCommitOnBranch(mergeCommit, matchedBranch);

        // Then
        Assertions.assertThat(mergeDateForCommitOnBranch).isEqualTo(mergeDateOnBranch);
    }
}
