package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.UserStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserService implements UserFacadeAdapter {

    private final UserStorageAdapter userStorageAdapter;

    @Override
    public User getOrCreateUserFromMail(String mail) {
        return userStorageAdapter.getUserFromMail(mail).orElseGet(() -> userStorageAdapter.createUserWithMail(mail));
    }

    @Override
    public User updateUserWithOrganization(User authenticatedUser, String externalId) throws CatleanException {
        return userStorageAdapter.updateUserWithOrganization(authenticatedUser, externalId);
    }
}
