package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.AccountStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import lombok.AllArgsConstructor;

import java.util.Optional;

import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper.entityToDomain;
import static java.util.UUID.randomUUID;

@AllArgsConstructor
public class PostgresUserAdapter implements AccountStorageAdapter {

    private final UserRepository userRepository;

    @Override
    public Optional<User> getUserFromMail(String mail) {
        return userRepository.findByMail(mail).map(UserMapper::entityToDomain);
    }

    @Override
    public User createUserWithMail(String mail) {
        final UserEntity userEntity = createUserFromMail(mail);
        return entityToDomain(userRepository.save(userEntity));
    }

    private static UserEntity createUserFromMail(String mail) {
        final UserEntity userEntity = new UserEntity();
        userEntity.setMail(mail);
        userEntity.setId(randomUUID().toString());
        return userEntity;
    }
}
