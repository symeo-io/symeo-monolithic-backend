package io.symeo.monolithic.backend.infrastructure.postgres.entity.account;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "organization_api_key", schema = "organization_storage")
@EntityListeners(AuditingEntityListener.class)
public class OrganizationApiKeyEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "key", nullable = false)
    private String key;
}
