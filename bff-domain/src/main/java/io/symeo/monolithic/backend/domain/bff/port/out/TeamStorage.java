package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.account.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamStorage {

    List<Team> createTeamsForUser(List<Team> teams, User user) throws SymeoException;

    List<Team> findByOrganization(Organization organization) throws SymeoException;
    Optional<Team> findById(UUID teamId) throws SymeoException;

    void deleteById(UUID teamId) throws SymeoException;

    void update(Team team) throws SymeoException;
}
