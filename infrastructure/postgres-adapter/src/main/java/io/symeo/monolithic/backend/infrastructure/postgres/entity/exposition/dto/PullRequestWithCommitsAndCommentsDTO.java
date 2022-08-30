package io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto;

import com.sun.istack.NotNull;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommentEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pull_request", schema = "exposition_storage")
@Entity
public class PullRequestWithCommitsAndCommentsDTO {

    @Id
    @Column(name = "id", nullable = false)
    @NotNull
    String id;
    @Column(name = "merge_date")
    Date mergeDate;
    @Column(name = "creation_date")
    Date creationDate;
    @Column(name = "state")
    String state;
    @OneToMany(mappedBy = "pullRequest", fetch = FetchType.EAGER)
    List<CommitEntity> commits;
    @OneToMany(mappedBy = "pullRequest", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    List<CommentEntity> comments;
}
