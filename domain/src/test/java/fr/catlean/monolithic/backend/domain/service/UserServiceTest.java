package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.AccountStorageAdapter;
import org.junit.jupiter.api.Test;

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
        final AccountStorageAdapter accountStorageAdapter = mock(AccountStorageAdapter.class);
        final UserService userService = new UserService(accountStorageAdapter);
        final String mail = faker.name().fullName();
        final User expectedUser =
                User.builder().id(UUID.randomUUID())
                        .mail(mail).build();

        // When
        when(accountStorageAdapter.getUserFromMail(mail)).thenReturn(Optional.of(expectedUser));
        final User user = userService.getOrCreateUserFromMail(mail);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getMail()).isEqualTo(mail);
    }

    @Test
    void should_not_get_user_and_create_it_and_return_it() {
        // Given
        final AccountStorageAdapter accountStorageAdapter = mock(AccountStorageAdapter.class);
        final UserService userService = new UserService(accountStorageAdapter);
        final String mail = faker.name().fullName();
        final User expectedUser =
                User.builder().id(UUID.randomUUID())
                        .mail(mail).build();

        // When
        when(accountStorageAdapter.getUserFromMail(mail)).thenReturn(Optional.empty());
        when(accountStorageAdapter.createUserWithMail(mail)).thenReturn(expectedUser);
        final User user = userService.getOrCreateUserFromMail(mail);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getMail()).isEqualTo(mail);
    }
}
