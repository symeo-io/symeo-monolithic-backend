package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account;

import fr.catlean.monolithic.backend.domain.model.Onboarding;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface OnboardingMapper {

    static OnboardingEntity domainToEntity(final Onboarding onboarding) {
        return OnboardingEntity.builder()
                .id(isNull(onboarding.getId()) ? UUID.randomUUID().toString() : onboarding.getId().toString())
                .hasConnectedToVcs(onboarding.getHasConnectedToVcs())
                .hasConfiguredTeam(onboarding.getHasConfiguredTeam())
                .build();
    }

    static Onboarding entityToDomain(final OnboardingEntity onboardingEntity) {
        return Onboarding.builder()
                .id(UUID.fromString(onboardingEntity.getId()))
                .hasConnectedToVcs(onboardingEntity.getHasConnectedToVcs())
                .hasConfiguredTeam(onboardingEntity.getHasConfiguredTeam())
                .build();
    }
}
