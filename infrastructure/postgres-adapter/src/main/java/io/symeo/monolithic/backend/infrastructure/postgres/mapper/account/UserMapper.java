package io.symeo.monolithic.backend.infrastructure.postgres.mapper.account;

import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;

import java.util.List;
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
        if (nonNull(userEntity.getOrganizationEntities()) && !userEntity.getOrganizationEntities().isEmpty()) {
            user = user.toBuilder().organizations(
                    List.of(
                            OrganizationMapper.entityToDomain(userEntity.getOrganizationEntities().get(0))
                    )
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
                .organizationEntities(isNull(user.getOrganization()) ? null :
                        List.of(OrganizationMapper.domainToEntity(user.getOrganization())))
                .onboardingEntity(isNull(user.getOnboarding()) ? null :
                        OnboardingMapper.domainToEntity(user.getOnboarding()))
                .status(user.getStatus())
                .build();
    }
}
