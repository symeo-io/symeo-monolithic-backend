package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStorageAdapter {
    Optional<User> getUserFromEmail(String email);

    User createUserWithEmail(String email) throws CatleanException;

    User updateUserWithOrganization(User authenticatedUser, String organizationExternalId) throws CatleanException;

    List<User> findAllByOrganization(Organization organization) throws CatleanException;

    List<User> saveUsers(List<User> users) throws CatleanException;

    User saveUser(User user) throws CatleanException;

    void removeOrganizationForUserId(UUID id) throws CatleanException;
}
