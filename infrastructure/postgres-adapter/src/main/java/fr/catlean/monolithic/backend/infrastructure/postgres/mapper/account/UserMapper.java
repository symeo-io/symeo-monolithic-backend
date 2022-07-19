package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account;

import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface UserMapper {

    static User entityToDomain(final UserEntity userEntity) {
        User user = User.builder()
                .id(UUID.fromString(userEntity.getId()))
                .mail(userEntity.getMail())
                .build();
        if (nonNull(userEntity.getOrganizationEntity())) {
            user = user.toBuilder().organization(
                    OrganizationMapper.entityToDomain(userEntity.getOrganizationEntity())
            ).build();
        }
        if (nonNull(userEntity.getOnboardingEntity())) {
            user = user.toBuilder()
                    .onboarding(
                            OnboardingMapper.entityToDomain(userEntity.getOnboardingEntity())
                    ).build();
        }
        return user;

    }

    static UserEntity domainToEntity(final User user) {
        return UserEntity.builder()
                .id(isNull(user.getId()) ? UUID.randomUUID().toString() : user.getId().toString())
                .mail(user.getMail())
                .onboardingEntity(isNull(user.getOnboarding()) ? null :
                        OnboardingMapper.domainToEntity(user.getOnboarding()))
                .build();
    }
}
