package fr.catlean.monolithic.backend.infrastructure.postgres.it.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresAccountAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.it.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresAccountAdapterTestIT {

    private final Faker faker = new Faker();
    @Autowired
    private UserRepository userRepository;


    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void should_create_user() {
        // Given
        final PostgresAccountAdapter postgresAccountAdapter = new PostgresAccountAdapter(userRepository);
        final String mail = faker.name().title();

        // When
        final User user = postgresAccountAdapter.createUserWithMail(mail);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        final Optional<UserEntity> optionalUserEntity = userRepository.findByMail(user.getMail());
        assertThat(optionalUserEntity).isPresent();
        final UserEntity userEntity = optionalUserEntity.get();
        assertThat(userEntity.getId()).isEqualTo(user.getId().toString());
        assertThat(userEntity.getMail()).isEqualTo(user.getMail());
    }

    @Test
    void should_read_user() {
        // Given
        final PostgresAccountAdapter postgresAccountAdapter = new PostgresAccountAdapter(userRepository);
        final String mail = faker.name().title();
        postgresAccountAdapter.createUserWithMail(mail);

        // When
        final Optional<User> existingUser = postgresAccountAdapter.getUserFromMail(mail);
        final Optional<User> notExistingUser = postgresAccountAdapter.getUserFromMail(faker.dragonBall().character());

        // Then
        assertThat(existingUser).isPresent();
        final User user = existingUser.get();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getMail()).isEqualTo(mail);
        assertThat(notExistingUser).isEmpty();

    }
}
