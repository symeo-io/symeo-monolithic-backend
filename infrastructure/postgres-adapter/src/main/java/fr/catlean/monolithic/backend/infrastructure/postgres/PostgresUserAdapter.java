package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.UserStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import lombok.AllArgsConstructor;

import java.util.Optional;

import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper.entityToDomain;
import static java.util.UUID.randomUUID;

@AllArgsConstructor
public class PostgresUserAdapter implements UserStorageAdapter {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public Optional<User> getUserFromMail(String mail) {
        return userRepository.findByMail(mail).map(UserMapper::entityToDomain);
    }

    @Override
    public User createUserWithMail(String mail) {
        final UserEntity userEntity = createUserFromMail(mail);
        return entityToDomain(userRepository.save(userEntity));
    }

    @Override
    public User updateUserWithOrganization(User authenticatedUser, String organizationExternalId) throws CatleanException {
        final UserEntity userEntity = UserMapper.domainToEntity(authenticatedUser);
        userEntity.setOrganizationEntity(organizationRepository.findByExternalId(organizationExternalId)
                .orElseThrow(() -> CatleanException.builder().code("F.ORGANIZATION_NOT_FOUND").message(
                        "Organization not found for externalId " + organizationExternalId).build()));
        return UserMapper.entityToDomain(userRepository.save(userEntity));


    }

    private static UserEntity createUserFromMail(String mail) {
        final UserEntity userEntity = new UserEntity();
        userEntity.setMail(mail);
        userEntity.setId(randomUUID().toString());
        return userEntity;
    }
}
