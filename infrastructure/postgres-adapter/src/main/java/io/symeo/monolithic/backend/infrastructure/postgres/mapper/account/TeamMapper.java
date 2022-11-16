package io.symeo.monolithic.backend.infrastructure.postgres.mapper.account;

import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface TeamMapper {

    static TeamEntity domainToEntity(final Team team) {
        return TeamEntity.builder()
                .id(isNull(team.getId()) ? UUID.randomUUID() : team.getId())
                .name(team.getName())
                .organizationId(team.getOrganizationId())
                .repositoryIds(team.getRepositories().stream().map(RepositoryView::getId).toList())
                .build();
    }

    static Team entityToDomain(TeamEntity teamEntity) {
        return Team.builder()
                .name(teamEntity.getName())
                .repositories(
                        teamEntity.getRepositoryIds().stream().map(id -> RepositoryView.builder().id(id).build()).toList()
                )
                .id(teamEntity.getId())
                .organizationId(teamEntity.getOrganizationId())
                .build();
    }
}
