package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStorageAdapter {
    Optional<User> getUserFromEmail(String email);

    User createUserWithEmail(String email) throws SymeoException;

    User updateUserWithOrganization(User authenticatedUser, String organizationExternalId) throws SymeoException;

    List<User> findAllByOrganization(Organization organization) throws SymeoException;

    List<User> saveUsers(List<User> users) throws SymeoException;

    User saveUser(User user) throws SymeoException;

    void removeOrganizationForUserId(UUID id) throws SymeoException;

    List<User> getUsersFromEmails(List<String> emails) throws SymeoException;
}
