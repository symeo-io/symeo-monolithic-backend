package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.account.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TeamFacadeAdapter {

    List<Team> createTeamsForNameAndRepositoriesAndUser(Map<String, List<String>> repositoryIdsMappedToTeamName,
                                                        User user) throws SymeoException;

    List<Team> getTeamsForOrganization(Organization organization) throws SymeoException;

    void deleteForId(UUID teamId) throws SymeoException;

    void update(Team team) throws SymeoException;
}
