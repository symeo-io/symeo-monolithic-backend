package io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "commit_testing_data", schema = "exposition_storage")
@EntityListeners(AuditingEntityListener.class)
public class CommitTestingDataEntity {
    @Id
    @Column(name = "id", nullable = false)
    UUID id;

    @Column(name = "coverage")
    Float coverage;

    @Column(name = "code_line_count")
    Integer codeLineCount;

    @Column(name = "test_line_count")
    Integer testLineCount;

    @Column(name = "test_count")
    Integer testCount;

    @Column(name = "test_type")
    String testType;

    @Column(name = "repository_name")
    String repositoryName;

    @Column(name = "branch_name")
    String branchName;

    @Column(name = "commit_sha")
    String commitSha;
}
