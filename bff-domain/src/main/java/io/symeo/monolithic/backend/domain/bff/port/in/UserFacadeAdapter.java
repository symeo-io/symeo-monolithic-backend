package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;
import java.util.UUID;

public interface UserFacadeAdapter {

    User getOrCreateUserFromEmail(String email) throws SymeoException;

    User updateUserWithOrganization(User authenticatedUser, String externalId) throws SymeoException;

    List<User> getAllUsersForOrganization(Organization organization) throws SymeoException;

    List<User> inviteUsersForOrganization(Organization organization, User authenticatedUser, List<User> users) throws SymeoException;

    void removeUserFromOrganization(UUID id, Organization organization) throws SymeoException;
}
