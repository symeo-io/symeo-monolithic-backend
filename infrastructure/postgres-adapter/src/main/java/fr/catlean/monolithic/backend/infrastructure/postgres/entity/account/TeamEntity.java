package fr.catlean.monolithic.backend.infrastructure.postgres.entity.account;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "team", schema = "account_storage")
public class TeamEntity extends AbstractEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "team_to_repository", schema = "exposition_storage",
            joinColumns = @JoinColumn(name = "team_id")
    )
    @Column(name = "repository_id")
    @Builder.Default
    List<String> repositoryIds = new ArrayList<>();


}
