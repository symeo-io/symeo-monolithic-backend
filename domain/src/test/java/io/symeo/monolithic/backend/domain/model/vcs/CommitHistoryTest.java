package io.symeo.monolithic.backend.domain.model.vcs;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.CommitHistory;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;

public class CommitHistoryTest {

    private static final Faker faker = new Faker();

    @Test
    void should_return_true_for_simple_parent_child_commits() {
        // Given
        final String sha1 = faker.rickAndMorty().character() + "-1";
        final String sha2 = faker.rickAndMorty().character() + "-2";
        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(
                List.of(

                        Commit.builder()
                                .sha(sha1)
                                .parentShaList(List.of(sha2, faker.name().firstName()))
                                .build(),
                        Commit.builder()
                                .sha(sha2)
                                .parentShaList(List.of())
                                .build()));

        // When
        final boolean commitPresentOnMergeCommitHistory = commitHistory.isCommitPresentOnMergeCommitHistory(sha2, sha1);

        // Then
        assertThat(commitPresentOnMergeCommitHistory).isTrue();
    }

    @Test
    void should_return_true_for_simple_parent_grand_child_commits() {
        // Given
        final String sha1 = faker.rickAndMorty().character() + "-1";
        final String sha2 = faker.rickAndMorty().character() + "-2";
        final String sha3 = faker.rickAndMorty().character() + "-3";
        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(
                List.of(

                        Commit.builder()
                                .sha(sha1)
                                .parentShaList(List.of(sha2, faker.name().firstName()))
                                .build(),
                        Commit.builder()
                                .sha(sha2)
                                .parentShaList(List.of(sha3, faker.name().firstName()))
                                .build(),
                        Commit.builder()
                                .sha(sha3)
                                .parentShaList(List.of())
                                .build()));

        // When
        final boolean commitPresentOnMergeCommitHistory = commitHistory.isCommitPresentOnMergeCommitHistory(sha3, sha1);

        // Then
        assertThat(commitPresentOnMergeCommitHistory).isTrue();
    }


    @Test
    void should_find_if_a_commit_is_present_in_a_given_commit_history() throws SymeoException {
        // Given
        final String mergeSha = faker.gameOfThrones().character();
        final Commit mergeCommit = Commit.builder()
                .sha(mergeSha)
                .date(stringToDate("2022-01-01"))
                .build();
        final String sha1 = faker.rickAndMorty().character() + "-1";
        final String sha2 = faker.rickAndMorty().character() + "-2";
        final String sha3 = faker.rickAndMorty().character() + "-3";
        final String sha4 = faker.rickAndMorty().character() + "-4";
        final Date mergeDateOnBranch = stringToDate("2022-01-15");
        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(
                List.of(

                        Commit.builder()
                                .sha(sha1)
                                .parentShaList(List.of(sha2, faker.name().firstName()))
                                .build(),
                        Commit.builder()
                                .sha(sha2)
                                .parentShaList(List.of(sha3, faker.name().lastName()))
                                .build(),
                        Commit.builder()
                                .sha(sha3)
                                .date(mergeDateOnBranch)
                                .parentShaList(List.of(sha4))
                                .build(),
                        Commit.builder()
                                .sha(sha4)
                                .parentShaList(List.of(mergeSha, faker.name().name()))
                                .build(),
                        mergeCommit
                )
        );

        // When
        final boolean commitPresentOnMergeCommitHistory =
                commitHistory.isCommitPresentOnMergeCommitHistory(mergeCommit.getSha(), sha3);

        // Then
        assertThat(commitPresentOnMergeCommitHistory).isTrue();
    }
}
