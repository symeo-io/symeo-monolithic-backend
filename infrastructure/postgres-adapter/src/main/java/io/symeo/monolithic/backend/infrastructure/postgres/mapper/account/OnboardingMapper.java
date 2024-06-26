package io.symeo.monolithic.backend.infrastructure.postgres.mapper.account;

import io.symeo.monolithic.backend.domain.bff.model.account.Onboarding;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface OnboardingMapper {

    static OnboardingEntity domainToEntity(final Onboarding onboarding) {
        return OnboardingEntity.builder()
                .id(isNull(onboarding.getId()) ? UUID.randomUUID() : onboarding.getId())
                .hasConnectedToVcs(onboarding.getHasConnectedToVcs())
                .hasConfiguredTeam(onboarding.getHasConfiguredTeam())
                .build();
    }

    static Onboarding entityToDomain(final OnboardingEntity onboardingEntity) {
        return Onboarding.builder()
                .id(onboardingEntity.getId())
                .hasConnectedToVcs(onboardingEntity.getHasConnectedToVcs())
                .hasConfiguredTeam(onboardingEntity.getHasConfiguredTeam())
                .build();
    }
}
