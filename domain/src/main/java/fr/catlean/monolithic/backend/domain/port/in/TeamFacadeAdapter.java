package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.List;
import java.util.Map;

public interface TeamFacadeAdapter {

    List<Team> createTeamsForNameAndRepositoriesAndUser(Map<String, List<Long>> repositoryIdsMappedToTeamName,
                                                        User user) throws CatleanException;
}
