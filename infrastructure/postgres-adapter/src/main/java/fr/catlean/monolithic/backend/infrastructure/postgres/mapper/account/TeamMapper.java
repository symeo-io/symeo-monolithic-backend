package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account;

import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface TeamMapper {

    static TeamEntity domainToEntity(final Team team) {
        return TeamEntity.builder()
                .id(isNull(team.getId()) ? UUID.randomUUID().toString() : team.getId().toString())
                .name(team.getName())
                .organizationId(team.getOrganizationId().toString())
                .repositoryIds(team.getRepositories().stream().map(Repository::getId).toList())
                .build();
    }

    static Team entityToDomain(TeamEntity teamEntity) {
        return Team.builder()
                .name(teamEntity.getName())
                .repositories(
                        teamEntity.getRepositoryIds().stream().map(id -> Repository.builder().id(id).build()).toList()
                )
                .id(UUID.fromString(teamEntity.getId()))
                .organizationId(UUID.fromString(teamEntity.getOrganizationId()))
                .build();
    }
}
