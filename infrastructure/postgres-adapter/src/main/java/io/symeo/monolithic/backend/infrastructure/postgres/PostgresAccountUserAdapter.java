package io.symeo.monolithic.backend.infrastructure.postgres;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.port.out.UserStorageAdapter;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.ORGANISATION_NOT_FOUND;
import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;

@AllArgsConstructor
@Slf4j
public class PostgresAccountUserAdapter implements UserStorageAdapter {

    private final UserRepository userRepository;
    private final VcsOrganizationRepository vcsOrganizationRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserFromEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::entityToDomain);
    }

    @Override
    public User createUserWithEmail(String email) throws SymeoException {
        try {
            final UserEntity userEntity = createUserFromMail(email);
            return UserMapper.entityToDomain(userRepository.save(userEntity));
        } catch (Exception e) {
            final String message = String.format("Failed to create user for mail %s", email);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public User updateUserWithOrganization(User authenticatedUser, String organizationExternalId) throws SymeoException {
        final UserEntity userEntity = UserMapper.domainToEntity(authenticatedUser);
        final VcsOrganizationEntity vcsOrganizationEntity =
                vcsOrganizationRepository.findByExternalId(organizationExternalId)
                        .orElseThrow(() -> SymeoException.builder().code(ORGANISATION_NOT_FOUND).message(
                                "Organization not found for externalId " + organizationExternalId).build());
        userEntity.setOrganizationEntities(List.of(vcsOrganizationEntity.getOrganizationEntity()));
        return UserMapper.entityToDomain(userRepository.save(userEntity));


    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllByOrganization(Organization organization) throws SymeoException {
        try {
            return userRepository.findAllForOrganizationId(organization.getId())
                    .stream().map(UserMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to find all users for organization %s", organization);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public List<User> saveUsers(List<User> users) throws SymeoException {
        try {
            return userRepository.saveAll(users.stream().map(UserMapper::domainToEntity).toList())
                    .stream().map(UserMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to save users %s", users);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public User saveUser(User user) throws SymeoException {
        try {
            return UserMapper.entityToDomain(userRepository.save(UserMapper.domainToEntity(user)));
        } catch (Exception e) {
            final String message = String.format("Failed to save user %s", user);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public void removeOrganizationForUserId(UUID id) throws SymeoException {
        try {
            final Optional<UserEntity> optionalUserEntity = userRepository.findById(id);
            if (optionalUserEntity.isPresent()) {
                final UserEntity userEntity = optionalUserEntity.get();
                userEntity.setOrganizationEntities(new ArrayList<>());
                userRepository.save(userEntity);
            } else {
                LOGGER.warn("User for id {} not found, failed to remove its organization", id);
            }
        } catch (Exception e) {
            final String message = String.format("Failed to remove organization for user id %s", id);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public List<User> getUsersFromEmails(List<String> emails) throws SymeoException {
        try {
            return userRepository.findAllByEmailIn(emails)
                    .stream().map(UserMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to users for emails %s", String.join(", ", emails));
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    private static UserEntity createUserFromMail(String mail) {
        return UserMapper.domainToEntity(User.builder().email(mail).build());
    }
}
