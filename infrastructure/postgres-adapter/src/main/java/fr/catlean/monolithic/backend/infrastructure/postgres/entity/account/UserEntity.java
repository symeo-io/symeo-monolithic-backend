package fr.catlean.monolithic.backend.infrastructure.postgres.entity.account;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;

import static java.util.UUID.randomUUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "user", schema = "account")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {


    @Id
    @Column(name = "id", nullable = false)
    String id;
    @Column(name = "mail", nullable = false)
    String mail;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;


}
