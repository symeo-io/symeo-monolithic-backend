package io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pull_request", schema = "exposition_storage")
@Entity
public class PullRequestFullViewDTO {

    @Id
    @Column(name = "id", nullable = false)
    @NotNull
    String id;
    @Column(name = "deleted_line_number")
    int deletedLineNumber;
    @Column(name = "added_line_number")
    int addedLineNumber;
    @Column(name = "creation_date")
    ZonedDateTime creationDate;
    @Column(name = "merge_date")
    ZonedDateTime mergeDate;
    @Column(name = "close_date")
    ZonedDateTime closeDate;
    @Column(name = "state")
    String state;
    @Column(name = "vcs_url")
    String vcsUrl;
    @Column(name = "head")
    String head;
    @Column(name = "base")
    String base;
    @Column(name = "title")
    String title;
    @Column(name = "commit_number")
    int commitNumber;
    @Column(name = "vcs_repository")
    String vcsRepository;
    @Column(name = "author_login")
    String authorLogin;
    @Column(name = "merge_commit_sha")
    String mergeCommitSha;
}
