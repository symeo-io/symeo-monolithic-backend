package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.UserStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.ORGANISATION_NOT_FOUND;
import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper.domainToEntity;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper.entityToDomain;

@AllArgsConstructor
@Slf4j
public class PostgresAccountUserAdapter implements UserStorageAdapter {

    private final UserRepository userRepository;
    private final VcsOrganizationRepository vcsOrganizationRepository;

    @Override
    public Optional<User> getUserFromEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::entityToDomain);
    }

    @Override
    public User createUserWithEmail(String email) throws CatleanException {
        try {
            final UserEntity userEntity = createUserFromMail(email);
            return entityToDomain(userRepository.save(userEntity));
        } catch (Exception e) {
            final String message = String.format("Failed to create user for mail %s", email);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public User updateUserWithOrganization(User authenticatedUser, String organizationExternalId) throws CatleanException {
        final UserEntity userEntity = domainToEntity(authenticatedUser);

        final VcsOrganizationEntity vcsOrganizationEntity =
                vcsOrganizationRepository.findByExternalId(organizationExternalId)
                        .orElseThrow(() -> CatleanException.builder().code(ORGANISATION_NOT_FOUND).message(
                                "Organization not found for externalId " + organizationExternalId).build());
        userEntity.setOrganizationEntity(vcsOrganizationEntity.getOrganizationEntity());
        return entityToDomain(userRepository.save(userEntity));


    }

    @Override
    public List<User> findAllByOrganization(Organization organization) throws CatleanException {
        try {
            return userRepository.findAllByOrganizationId(organization.getId().toString())
                    .stream().map(UserMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to find all users for organization %s", organization);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public List<User> saveUsers(List<User> users) throws CatleanException {
        try {
            return userRepository.saveAll(users.stream().map(UserMapper::domainToEntity).toList())
                    .stream().map(UserMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to save users %s", users);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public User saveUser(User user) throws CatleanException {
        try {
            return entityToDomain(userRepository.save(domainToEntity(user)));
        } catch (Exception e) {
            final String message = String.format("Failed to save user %s", user);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public void removeOrganizationForUserId(UUID id) throws CatleanException {
        try {
            final Optional<UserEntity> optionalUserEntity = userRepository.findById(id.toString());
            if (optionalUserEntity.isPresent()) {
                final UserEntity userEntity = optionalUserEntity.get();
                userEntity.setOrganizationEntity(null);
                userRepository.save(userEntity);
            } else {
                LOGGER.warn("User for id {} not found, failed to remove its organization", id);
            }
        } catch (Exception e) {
            final String message = String.format("Failed to remove organization for user id %s", id);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    private static UserEntity createUserFromMail(String mail) {
        return domainToEntity(User.builder().email(mail).build());
    }
}
