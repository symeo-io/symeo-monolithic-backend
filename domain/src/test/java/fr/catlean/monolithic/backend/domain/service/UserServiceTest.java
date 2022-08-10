package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Onboarding;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.port.out.EmailDeliveryAdapter;
import fr.catlean.monolithic.backend.domain.port.out.UserStorageAdapter;
import fr.catlean.monolithic.backend.domain.service.account.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_return_existing_user() throws CatleanException {
        // Given
        final UserStorageAdapter userStorageAdapter = mock(UserStorageAdapter.class);
        final EmailDeliveryAdapter emailDeliveryAdapter = mock(EmailDeliveryAdapter.class);
        final UserService userService = new UserService(userStorageAdapter, emailDeliveryAdapter);
        final String mail = faker.name().fullName();
        final User expectedUser =
                User.builder().id(UUID.randomUUID()).status(User.ACTIVE)
                        .email(mail).build();

        // When
        when(userStorageAdapter.getUserFromEmail(mail)).thenReturn(Optional.of(expectedUser));
        final User user = userService.getOrCreateUserFromEmail(mail);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(mail);
    }

    @Test
    void should_update_existing_pending_user_to_active_user_given_an_email() throws CatleanException {
        // Given
        final UserStorageAdapter userStorageAdapter = mock(UserStorageAdapter.class);
        final EmailDeliveryAdapter emailDeliveryAdapter = mock(EmailDeliveryAdapter.class);
        final UserService userService = new UserService(userStorageAdapter, emailDeliveryAdapter);
        final String mail = faker.name().fullName();
        final User pendingUser =
                User.builder().id(UUID.randomUUID())
                        .email(mail).build();

        // When
        when(userStorageAdapter.getUserFromEmail(mail)).thenReturn(Optional.of(pendingUser));
        final ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userStorageAdapter.saveUser(userArgumentCaptor.capture())).thenReturn(pendingUser.isActive());
        final User user = userService.getOrCreateUserFromEmail(mail);

        // Then
        verify(userStorageAdapter, times(1)).saveUser(any());
        assertThat(userArgumentCaptor.getValue().getStatus()).isEqualTo(User.ACTIVE);
        assertThat(user).isEqualTo(pendingUser.isActive());
    }

    @Test
    void should_not_get_user_and_create_it_and_return_it() throws CatleanException {
        // Given
        final UserStorageAdapter userStorageAdapter = mock(UserStorageAdapter.class);
        final EmailDeliveryAdapter emailDeliveryAdapter = mock(EmailDeliveryAdapter.class);
        final UserService userService = new UserService(userStorageAdapter, emailDeliveryAdapter);
        final String mail = faker.name().fullName();
        final User expectedUser =
                User.builder().id(UUID.randomUUID())
                        .email(mail)
                        .onboarding(Onboarding.builder().id(UUID.randomUUID()).build())
                        .build();

        // When
        when(userStorageAdapter.getUserFromEmail(mail)).thenReturn(Optional.empty());
        when(userStorageAdapter.createUserWithEmail(mail)).thenReturn(expectedUser);
        final User user = userService.getOrCreateUserFromEmail(mail);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(mail);
    }


    @Test
    void should_update_user_with_organization() throws CatleanException {
        // Given
        final UserStorageAdapter userStorageAdapter = mock(UserStorageAdapter.class);
        final EmailDeliveryAdapter emailDeliveryAdapter = mock(EmailDeliveryAdapter.class);
        final UserService userService = new UserService(userStorageAdapter, emailDeliveryAdapter);
        final String externalId = faker.dragonBall().character();
        final User authenticatedUser = User.builder()
                .id(UUID.randomUUID()).email(faker.dragonBall().character()).build();
        final String name = faker.pokemon().name();
        final Organization expectedOrganization = Organization.builder()
                .name(name)
                .id(UUID.randomUUID())
                .vcsOrganization(
                        VcsOrganization.builder()
                                .externalId(externalId)
                                .name(name)
                                .build()
                )
                .build();


        // When
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> externalIdCaptor = ArgumentCaptor.forClass(String.class);
        when(userStorageAdapter.updateUserWithOrganization(userArgumentCaptor.capture(), externalIdCaptor.capture()))
                .thenReturn(authenticatedUser.toBuilder().organizations(List.of(expectedOrganization)).build());
        userService.updateUserWithOrganization(authenticatedUser,
                externalId);

        // Then
        assertThat(userArgumentCaptor.getValue().getId()).isEqualTo(authenticatedUser.getId());
        assertThat(userArgumentCaptor.getValue().getOrganization()).isEqualTo(authenticatedUser.getOrganization());
        assertThat(userArgumentCaptor.getValue().getOnboarding().getHasConnectedToVcs()).isEqualTo(authenticatedUser.getOnboarding().getHasConnectedToVcs());
        assertThat(externalIdCaptor.getValue()).isEqualTo(externalId);
    }

    @Test
    void should_get_all_users_given_an_organization() throws CatleanException {
        // Given
        final UserStorageAdapter userStorageAdapter = mock(UserStorageAdapter.class);
        final EmailDeliveryAdapter emailDeliveryAdapter = mock(EmailDeliveryAdapter.class);
        final UserService userService = new UserService(userStorageAdapter, emailDeliveryAdapter);
        final List<User> users = List.of(
                User.builder()
                        .email(faker.dragonBall().character())
                        .build(),
                User.builder()
                        .email(faker.dragonBall().character())
                        .build(),
                User.builder()
                        .email(faker.dragonBall().character())
                        .build()
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();

        // When
        when(userStorageAdapter.findAllByOrganization(organization))
                .thenReturn(users);
        final List<User> allUsersForOrganization =
                userService.getAllUsersForOrganization(organization);

        // Then
        assertThat(allUsersForOrganization).isEqualTo(users);
    }

    @Test
    void should_create_users_and_send_mail() throws CatleanException {
        // Given
        final User authenticatedUser = User.builder().id(UUID.randomUUID()).build();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UserStorageAdapter userStorageAdapter = mock(UserStorageAdapter.class);
        final EmailDeliveryAdapter emailDeliveryAdapter = mock(EmailDeliveryAdapter.class);
        final UserService userService = new UserService(userStorageAdapter, emailDeliveryAdapter);
        final List<User> users = List.of(
                User.builder()
                        .email(faker.dragonBall().character())
                        .build(),
                User.builder()
                        .email(faker.gameOfThrones().character())
                        .build(),
                User.builder()
                        .email(faker.harryPotter().character())
                        .build()
        );

        // When
        final ArgumentCaptor<List<User>> usersCaptor = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<Organization> organizationArgumentCaptor = ArgumentCaptor.forClass(Organization.class);
        final ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userStorageAdapter.saveUsers(usersCaptor.capture()))
                .thenReturn(users.stream().map(user -> user.toBuilder().id(UUID.randomUUID()).build()).toList());
        userService.inviteUsersForOrganization(organization, authenticatedUser, users);

        // Then
        verify(emailDeliveryAdapter, times(1)).sendInvitationForUsers(organizationArgumentCaptor.capture(),
                userArgumentCaptor.capture(), usersCaptor.capture());
        assertThat(usersCaptor.getAllValues()).hasSize(2);
        assertThat(organizationArgumentCaptor.getValue()).isEqualTo(organization);
        assertThat(userArgumentCaptor.getValue()).isEqualTo(authenticatedUser);
        usersCaptor.getAllValues().get(0).forEach(user -> assertThat(user.getId()).isNull());
        usersCaptor.getAllValues().get(0).forEach(user -> assertThat(user.getOrganization()).isEqualTo(organization));
        usersCaptor.getAllValues().get(0).forEach(user -> assertThat(user.getOnboarding().getHasConfiguredTeam()).isTrue());
        usersCaptor.getAllValues().get(0).forEach(user -> assertThat(user.getOnboarding().getHasConnectedToVcs()).isTrue());
        usersCaptor.getAllValues().get(1).forEach(user -> assertThat(user.getStatus()).isEqualTo(User.PENDING));
    }

    @Test
    void should_remove_user_for_id() throws CatleanException {
        // Given
        final UserStorageAdapter userStorageAdapter = mock(UserStorageAdapter.class);
        final EmailDeliveryAdapter emailDeliveryAdapter = mock(EmailDeliveryAdapter.class);
        final UserService userService = new UserService(userStorageAdapter, emailDeliveryAdapter);
        final UUID id = UUID.randomUUID();

        // When
        userService.removeUserFromOrganization(id, Organization.builder().build());

        // Then
        final ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(userStorageAdapter, times(1)).removeOrganizationForUserId(uuidArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getValue()).isEqualTo(id);
    }
}
