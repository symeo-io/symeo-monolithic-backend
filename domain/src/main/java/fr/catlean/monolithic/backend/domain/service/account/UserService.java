package fr.catlean.monolithic.backend.domain.service.account;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.EmailDeliveryAdapter;
import fr.catlean.monolithic.backend.domain.port.out.UserStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class UserService implements UserFacadeAdapter {

    private final UserStorageAdapter userStorageAdapter;
    private final EmailDeliveryAdapter emailDeliveryAdapter;

    @Override
    public User getOrCreateUserFromEmail(String email) throws CatleanException {
        final Optional<User> optionalUser = userStorageAdapter.getUserFromEmail(email);
        if (optionalUser.isPresent()) {
            final User user = optionalUser.get();
            if (user.getStatus().equals(User.PENDING)) {
                return userStorageAdapter.saveUser(user.isActive());
            }
            return user;
        } else {
            return userStorageAdapter.createUserWithEmail(email);
        }
    }

    @Override
    public User updateUserWithOrganization(User authenticatedUser, String externalId) throws CatleanException {
        authenticatedUser.hasConnectedToVcs();
        return userStorageAdapter.updateUserWithOrganization(authenticatedUser, externalId);
    }

    @Override
    public List<User> getAllUsersForOrganization(Organization organization) throws CatleanException {
        return userStorageAdapter.findAllByOrganization(organization);
    }

    @Override
    public List<User> createUsersForOrganization(Organization organization, List<User> users) throws CatleanException {
        final List<User> createdUsers = userStorageAdapter.saveUsers(users);
        emailDeliveryAdapter.sendInvitationForUsers(createdUsers);
        return users;
    }

    @Override
    public void removeUserFromOrganization(UUID id, Organization organization) throws CatleanException {
        userStorageAdapter.removeOrganizationForUserId(id);
    }
}
