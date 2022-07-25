package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.List;

public interface UserFacadeAdapter {

    User getOrCreateUserFromEmail(String email) throws CatleanException;

    User updateUserWithOrganization(User authenticatedUser, String externalId) throws CatleanException;

    List<User> getAllUsersForOrganization(Organization organization) throws CatleanException;

    List<User> createUsersForOrganization(Organization organization, List<User> users) throws CatleanException;

}
