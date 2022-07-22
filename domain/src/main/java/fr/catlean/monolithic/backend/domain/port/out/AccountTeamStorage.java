package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.List;

public interface AccountTeamStorage {

    List<Team> createTeamsForUser(List<Team> teams, User user) throws CatleanException;

    List<Team> findByOrganization(Organization organization) throws CatleanException;
}
