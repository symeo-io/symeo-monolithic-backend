package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.Team;
import io.symeo.monolithic.backend.domain.model.account.User;

import java.util.List;
import java.util.UUID;

public interface TeamStorage {

    List<Team> createTeamsForUser(List<Team> teams, User user) throws SymeoException;

    List<Team> findByOrganization(Organization organization) throws SymeoException;

    void deleteById(UUID teamId) throws SymeoException;

    void update(Team team) throws SymeoException;
}
