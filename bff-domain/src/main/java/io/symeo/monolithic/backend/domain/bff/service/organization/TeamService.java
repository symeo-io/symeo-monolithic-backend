package io.symeo.monolithic.backend.domain.bff.service.organization;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.in.TeamFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.BffSymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.TeamStorage;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import lombok.AllArgsConstructor;

import java.util.*;

@AllArgsConstructor
public class TeamService implements TeamFacadeAdapter {

    private final TeamStorage teamStorage;
    private final BffSymeoDataProcessingJobApiAdapter bffSymeoDataProcessingJobApiAdapter;
    private final OrganizationSettingsService organizationSettingsService;
    private final OrganizationStorageAdapter organizationStorageAdapter;

    @Override
    public List<Team> createTeamsForNameAndRepositoriesAndUser(final Map<String, List<String>> repositoryIdsMappedToTeamName,
                                                               User user) throws SymeoException {
        user.hasConfiguredTeam();
        final OrganizationSettings organizationSettings = organizationSettingsService.getOrganizationSettingsForOrganization(user.getOrganization());
        final String deployDetectionType = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getDeployDetectionType().value;
        final String pullRequestMergedOnBranchRegex = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex();
        final String tagRegex = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex();
        final List<String> excludeBranchRegexes = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
        final List<Team> teams = new ArrayList<>();
        repositoryIdsMappedToTeamName.forEach((teamName, repositoryIds) -> teams.add(
                Team.builder()
                        .name(teamName)
                        .repositories(repositoryIds.stream().map(id -> RepositoryView.builder().id(id).build()).toList())
                        .organizationId(user.getOrganization().getId())
                        .build()
        ));

        final List<Team> createdTeams = teamStorage.createTeamsForUser(teams, user);
        for (Team createdTeam : createdTeams) {
            bffSymeoDataProcessingJobApiAdapter.startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(user.getOrganization().getId(),
                    createdTeam.getId(), createdTeam.getRepositories().stream().map(RepositoryView::getId).toList(),
                    deployDetectionType, pullRequestMergedOnBranchRegex, tagRegex, excludeBranchRegexes);
        }
        return createdTeams;
    }

    @Override
    public List<Team> getTeamsForOrganization(Organization organization) throws SymeoException {
        return teamStorage.findByOrganization(organization);
    }

    @Override
    public void deleteForId(UUID teamId) throws SymeoException {
        teamStorage.deleteById(teamId);
    }

    @Override
    public void update(Team team) throws SymeoException {
        final Team teamUpdated = teamStorage.update(team);
        final Optional<Organization> organization = organizationStorageAdapter.findOrganizationById(teamUpdated.getOrganizationId());
        final OrganizationSettings organizationSettings = organizationSettingsService.getOrganizationSettingsForOrganization(organization.get());
        final String deployDetectionType = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getDeployDetectionType().value;
        final String pullRequestMergedOnBranchRegex = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getPullRequestMergedOnBranchRegex();
        final String tagRegex = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getTagRegex();
        final List<String> excludeBranchRegexes = organizationSettings.getDeliverySettings().getDeployDetectionSettings().getExcludeBranchRegexes();
        bffSymeoDataProcessingJobApiAdapter.startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(
                teamUpdated.getOrganizationId(), teamUpdated.getId(),
                teamUpdated.getRepositories().stream().map(RepositoryView::getId).toList(),
                deployDetectionType,
                pullRequestMergedOnBranchRegex,
                tagRegex,
                excludeBranchRegexes
        );
    }
}
