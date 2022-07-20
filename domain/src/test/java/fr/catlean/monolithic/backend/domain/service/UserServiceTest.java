package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Onboarding;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.UserStorageAdapter;
import fr.catlean.monolithic.backend.domain.service.account.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_return_existing_user() {
        // Given
        final UserStorageAdapter userStorageAdapter = mock(UserStorageAdapter.class);
        final UserService userService = new UserService(userStorageAdapter);
        final String mail = faker.name().fullName();
        final User expectedUser =
                User.builder().id(UUID.randomUUID())
                        .mail(mail).build();

        // When
        when(userStorageAdapter.getUserFromMail(mail)).thenReturn(Optional.of(expectedUser));
        final User user = userService.getOrCreateUserFromMail(mail);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getMail()).isEqualTo(mail);
    }

    @Test
    void should_not_get_user_and_create_it_and_return_it() {
        // Given
        final UserStorageAdapter userStorageAdapter = mock(UserStorageAdapter.class);
        final UserService userService = new UserService(userStorageAdapter);
        final String mail = faker.name().fullName();
        final User expectedUser =
                User.builder().id(UUID.randomUUID())
                        .mail(mail)
                        .onboarding(Onboarding.builder().id(UUID.randomUUID()).build())
                        .build();

        // When
        when(userStorageAdapter.getUserFromMail(mail)).thenReturn(Optional.empty());
        when(userStorageAdapter.createUserWithMail(mail)).thenReturn(expectedUser);
        final User user = userService.getOrCreateUserFromMail(mail);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getMail()).isEqualTo(mail);
    }


    @Test
    void should_update_user_with_organization() throws CatleanException {
        // Given
        final UserStorageAdapter userStorageAdapter = mock(UserStorageAdapter.class);
        final UserService userService = new UserService(userStorageAdapter);
        final String externalId = faker.dragonBall().character();
        final User authenticatedUser = User.builder()
                .id(UUID.randomUUID()).mail(faker.dragonBall().character()).build();
        final String name = faker.pokemon().name();
        final Organization expectedOrganization = Organization.builder()
                .name(name)
                .id(UUID.randomUUID())
                .externalId(externalId)
                .vcsConfiguration(
                        VcsConfiguration.builder()
                                .organizationName(name)
                                .build()
                )
                .build();


        // When
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> externalIdCaptor = ArgumentCaptor.forClass(String.class);
        when(userStorageAdapter.updateUserWithOrganization(userArgumentCaptor.capture(), externalIdCaptor.capture()))
                .thenReturn(authenticatedUser.toBuilder().organization(expectedOrganization).build());
        userService.updateUserWithOrganization(authenticatedUser,
                externalId);

        // Then
        assertThat(userArgumentCaptor.getValue().getId()).isEqualTo(authenticatedUser.getId());
        assertThat(userArgumentCaptor.getValue().getOrganization()).isEqualTo(authenticatedUser.getOrganization());
        assertThat(userArgumentCaptor.getValue().getOnboarding().getHasConnectedToVcs()).isEqualTo(authenticatedUser.getOnboarding().getHasConnectedToVcs());
        assertThat(externalIdCaptor.getValue()).isEqualTo(externalId);
    }
}
