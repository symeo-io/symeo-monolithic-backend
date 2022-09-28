package io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "tag", schema = "exposition_storage")
@EntityListeners(AuditingEntityListener.class)
public class TagEntity {

    @Id
    @Column(name = "sha", nullable = false)
    @NotNull
    String sha;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "repository_id", nullable = false)
    String repositoryId;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;
}
