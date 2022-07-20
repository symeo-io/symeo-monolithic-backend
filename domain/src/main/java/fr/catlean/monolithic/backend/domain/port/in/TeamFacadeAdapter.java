package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.List;

public interface TeamFacadeAdapter {
    Team createTeamForNameAndRepositoriesAndUser(String teamName, List<Integer> repositoryIds,
                                                 User user) throws CatleanException;
}
