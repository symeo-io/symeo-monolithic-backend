package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.Optional;

public interface UserStorageAdapter {
    Optional<User> getUserFromMail(String mail);

    User createUserWithMail(String mail);

    User updateUserWithOrganization(User authenticatedUser, String organizationExternalId) throws CatleanException;

}
