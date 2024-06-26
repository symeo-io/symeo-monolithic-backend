package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.bff.contract.api.model.*;

import java.util.List;

import static java.util.Objects.nonNull;

public interface UserContractMapper {

    static CurrentUserResponseContract currentUserToResponse(final User user) {
        final CurrentUserResponseContract currentUserResponseContract = new CurrentUserResponseContract();
        currentUserResponseContract.setUser(userToResponseContract(user));
        return currentUserResponseContract;
    }

    private static UserContract userToResponseContract(final User user) {
        final UserContract userContract = new UserContract();
        userContract.setEmail(user.getEmail());
        userContract.setId(user.getId());
        if (nonNull(user.getOrganization())) {
            userContract.setOrganization(organizationToContract(user.getOrganization()));
        }
        if (nonNull(user.getOnboarding())) {
            userContract.setOnboarding(onboardingToContract(user.getOnboarding()));
        }
        return userContract;
    }

    private static OrganizationContract organizationToContract(final Organization organization) {
        final OrganizationContract organizationContract = new OrganizationContract();
        organizationContract.setId(organization.getId());
        organizationContract.setName(organization.getName());
        return organizationContract;
    }

    private static OnboardingContract onboardingToContract(final Onboarding onboarding) {
        final OnboardingContract onboardingContract = new OnboardingContract();
        onboardingContract.setHasConnectedToVcs(onboarding.getHasConnectedToVcs());
        onboardingContract.setHasConfiguredTeam(onboarding.getHasConfiguredTeam());
        return onboardingContract;
    }

    static UsersResponseContract usersToResponse(final List<User> users) {
        final UsersResponseContract usersResponseContract = new UsersResponseContract();
        usersResponseContract.setUsers(users.stream()
                .map(UserContractMapper::userToResponse)
                .toList());
        return usersResponseContract;
    }

    static UsersResponseContract usersToError(final SymeoException symeoException) {
        final UsersResponseContract usersResponseContract = new UsersResponseContract();
        usersResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return usersResponseContract;
    }

    static CurrentUserResponseContract currentUsersToError(final SymeoException symeoException) {
        final CurrentUserResponseContract currentUserResponseContract = new CurrentUserResponseContract();
        currentUserResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return currentUserResponseContract;
    }

    static DeleteUserResponseContract exceptionToContract(final SymeoException symeoException) {
        final DeleteUserResponseContract deletedUserResponseContract = new DeleteUserResponseContract();
        deletedUserResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return deletedUserResponseContract;
    }

    private static UserResponseContract userToResponse(final User user) {
        final UserResponseContract userResponseContract = new UserResponseContract();
        userResponseContract.setEmail(user.getEmail());
        userResponseContract.setStatus(user.getStatus());
        userResponseContract.setId(user.getId());
        return userResponseContract;
    }

    static List<User> contractToUsers(final List<UserRequestContract> userRequestContract) {
        return userRequestContract.stream().map(UserContractMapper::contractToUser).toList();
    }

    private static User contractToUser(final UserRequestContract userRequestContract) {
        return User.builder().email(userRequestContract.getEmail()).build();
    }
}
