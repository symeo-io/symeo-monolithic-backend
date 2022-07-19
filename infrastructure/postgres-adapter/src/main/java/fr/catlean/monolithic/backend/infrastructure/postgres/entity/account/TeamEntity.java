package fr.catlean.monolithic.backend.infrastructure.postgres.entity.account;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "team", schema = "account")
public class TeamEntity extends AbstractEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @ElementCollection
    @CollectionTable(
            name = "team_to_repository", schema = "exposition_storage",
            joinColumns = @JoinColumn(name = "team_id")
    )
    @Column(name = "repository_id")
    @Builder.Default
    List<Integer> repositoryIds = new ArrayList<>();


}
