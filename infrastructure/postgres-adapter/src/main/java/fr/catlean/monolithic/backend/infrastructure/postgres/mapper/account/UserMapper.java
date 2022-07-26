package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account;

import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;

import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public interface UserMapper {

    static User entityToDomain(final UserEntity userEntity) {
        User user = User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .status(userEntity.getStatus())
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
                .id(isNull(user.getId()) ? UUID.randomUUID() : user.getId())
                .email(user.getEmail())
                .organizationEntity(isNull(user.getOrganization()) ? null :
                        OrganizationMapper.domainToEntity(user.getOrganization()))
                .onboardingEntity(isNull(user.getOnboarding()) ? null :
                        OnboardingMapper.domainToEntity(user.getOnboarding()))
                .status(user.getStatus())
                .build();
    }
}
