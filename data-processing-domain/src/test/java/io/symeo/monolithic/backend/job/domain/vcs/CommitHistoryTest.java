package io.symeo.monolithic.backend.job.domain.vcs;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.vcs.Commit;
import io.symeo.monolithic.backend.job.domain.model.vcs.CommitHistory;
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

    @Test
    void should_return_false_for_complex_parent_grand_child_commit() throws SymeoException {
        // Given
        final String mergeShaForDeploy = faker.gameOfThrones().character();

        final String sha1 = faker.rickAndMorty().character() + "-1";
        final String sha2 = faker.rickAndMorty().character() + "-2";
        final String sha3 = faker.rickAndMorty().character() + "-3";
        final String sha4 = faker.rickAndMorty().character() + "-4";
        final String sha5 = faker.rickAndMorty().character() + "-5";
        final String sha6 = faker.rickAndMorty().character() + "-6";
        final String sha7 = faker.rickAndMorty().character() + "-7";
        final String sha8 = faker.rickAndMorty().character() + "-8";
        final String sha9 = faker.rickAndMorty().character() + "-9";
        final String sha10 = faker.rickAndMorty().character() + "-10";
        final String sha11 = faker.rickAndMorty().character() + "-11";

        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(
                List.of(

                        Commit.builder()
                                .sha(mergeShaForDeploy)
                                .parentShaList(List.of(sha10, sha1))
                                .build(),
                        Commit.builder()
                                .sha(sha1)
                                .parentShaList(List.of(sha2))
                                .build(),
                        Commit.builder()
                                .sha(sha2)
                                .parentShaList(List.of(sha3))
                                .build(),
                        Commit.builder()
                                .sha(sha3)
                                .parentShaList(List.of(sha4, sha9))
                                .build(),
                        Commit.builder()
                                .sha(sha4)
                                .parentShaList(List.of(sha5))
                                .build(),
                        Commit.builder()
                                .sha(sha5)
                                .parentShaList(List.of(sha6))
                                .build(),
                        Commit.builder()
                                .sha(sha6)
                                .parentShaList(List.of(sha7))
                                .build(),
                        Commit.builder()
                                .sha(sha7)
                                .parentShaList(List.of(sha8))
                                .build(),
                        Commit.builder()
                                .sha(sha8)
                                .parentShaList(List.of())
                                .build(),
                        Commit.builder()
                                .sha(sha9)
                                .parentShaList(List.of(sha6))
                                .build(),
                        Commit.builder()
                                .sha(sha10)
                                .parentShaList(List.of(sha11))
                                .build(),
                        Commit.builder()
                                .sha(sha11)
                                .parentShaList(List.of(sha8))
                                .build()
                )
        );

        // When
        final boolean commitPresentOnMergeCommitHistory = commitHistory.isCommitPresentOnMergeCommitHistory(sha3, mergeShaForDeploy);

        // Then
        assertThat(commitPresentOnMergeCommitHistory).isTrue();

    }
}
