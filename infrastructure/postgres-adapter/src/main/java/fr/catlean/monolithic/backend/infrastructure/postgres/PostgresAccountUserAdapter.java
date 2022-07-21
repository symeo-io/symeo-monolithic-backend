package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.UserStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import lombok.AllArgsConstructor;

import java.util.Optional;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.ORGANISATION_NOT_FOUND;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper.domainToEntity;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper.entityToDomain;

@AllArgsConstructor
public class PostgresAccountUserAdapter implements UserStorageAdapter {

    private final UserRepository userRepository;
    private final VcsOrganizationRepository vcsOrganizationRepository;

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
        final UserEntity userEntity = domainToEntity(authenticatedUser);

        final VcsOrganizationEntity vcsOrganizationEntity =
                vcsOrganizationRepository.findByExternalId(organizationExternalId)
                        .orElseThrow(() -> CatleanException.builder().code(ORGANISATION_NOT_FOUND).message(
                                "Organization not found for externalId " + organizationExternalId).build());
        userEntity.setOrganizationEntity(vcsOrganizationEntity.getOrganizationEntity());
        return entityToDomain(userRepository.save(userEntity));


    }

    private static UserEntity createUserFromMail(String mail) {
        return domainToEntity(User.builder().mail(mail).build());
    }
}
