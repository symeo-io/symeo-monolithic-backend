package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TeamFacadeAdapter {

    List<Team> createTeamsForNameAndRepositoriesAndUser(Map<String, List<String>> repositoryIdsMappedToTeamName,
                                                        User user) throws CatleanException;

    List<Team> getTeamsForOrganization(Organization organization) throws CatleanException;

    void deleteForId(UUID teamId) throws CatleanException;

    void update(Team team) throws CatleanException;
}
