package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.List;
import java.util.UUID;

public interface UserFacadeAdapter {

    User getOrCreateUserFromEmail(String email) throws CatleanException;

    User updateUserWithOrganization(User authenticatedUser, String externalId) throws CatleanException;

    List<User> getAllUsersForOrganization(Organization organization) throws CatleanException;

    List<User> inviteUsersForOrganization(Organization organization, User authenticatedUser, List<User> users) throws CatleanException;

    void removeUserFromOrganization(UUID id, Organization organization) throws CatleanException;
}
