package io.symeo.monolithic.backend.domain.service.account;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.port.in.UserFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.EmailDeliveryAdapter;
import io.symeo.monolithic.backend.domain.port.out.UserStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class UserService implements UserFacadeAdapter {

    private final UserStorageAdapter userStorageAdapter;
    private final EmailDeliveryAdapter emailDeliveryAdapter;

    @Override
    public User getOrCreateUserFromEmail(String email) throws SymeoException {
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
    public User updateUserWithOrganization(User authenticatedUser, String externalId) throws SymeoException {
        authenticatedUser.hasConnectedToVcs();
        return userStorageAdapter.updateUserWithOrganization(authenticatedUser, externalId);
    }

    @Override
    public List<User> getAllUsersForOrganization(Organization organization) throws SymeoException {
        return userStorageAdapter.findAllByOrganization(organization);
    }

    @Override
    public List<User> inviteUsersForOrganization(final Organization organization, final User authenticatedUser,
                                                 final List<User> users) throws SymeoException {
        final List<User> existingUsers =
                userStorageAdapter.getUsersFromEmails(users.stream().map(User::getEmail).toList());
        final List<String> existingEmails = existingUsers.stream().map(User::getEmail).toList();
        final List<User> newUsers =
                users.stream().filter(user -> !existingEmails.contains(user.getEmail())).collect(Collectors.toList());
        // TODO : handle other use cases
        final List<User> existingNewUsersToUpdate =
                existingUsers.stream().filter(user -> isNull(user.getOrganization()) && user.getStatus().equals(User.PENDING))
                        .toList();
        newUsers.addAll(updateOnboardingForExistingNewUsers(existingNewUsersToUpdate));
        final List<User> createdUsers =
                userStorageAdapter.saveUsers(updateNewUserWithOrganizationAndOnboarding(organization, newUsers));
        emailDeliveryAdapter.sendInvitationForUsers(organization, authenticatedUser, createdUsers);
        return createdUsers;
    }

    private List<User> updateOnboardingForExistingNewUsers(List<User> existingNewUsersToUpdate) {
        return existingNewUsersToUpdate.stream()
                .map(user -> user.toBuilder()
                        .onboarding(
                                user.getOnboarding().toBuilder()
                                        .hasConfiguredTeam(true)
                                        .hasConnectedToVcs(true)
                                        .build()
                        )
                        .build()).toList();
    }

    private static List<User> updateNewUserWithOrganizationAndOnboarding(Organization organization,
                                                                         List<User> newUsers) {
        return newUsers.stream().map(user -> user.toBuilder()
                .organizations(List.of(organization))
                .onboarding(
                        Onboarding.builder()
                                .hasConfiguredTeam(true)
                                .hasConnectedToVcs(true)
                                .build())
                .build()).toList();
    }

    @Override
    public void removeUserFromOrganization(UUID id, Organization organization) throws SymeoException {
        userStorageAdapter.removeOrganizationForUserId(id);
    }
}
