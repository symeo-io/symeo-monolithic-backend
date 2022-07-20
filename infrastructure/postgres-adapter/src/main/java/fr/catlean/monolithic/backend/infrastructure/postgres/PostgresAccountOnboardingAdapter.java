package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Onboarding;
import fr.catlean.monolithic.backend.domain.port.out.AccountOnboardingStorage;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OnboardingRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OnboardingMapper.domainToEntity;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OnboardingMapper.entityToDomain;

@AllArgsConstructor
@Slf4j
public class PostgresAccountOnboardingAdapter implements AccountOnboardingStorage {

    private final OnboardingRepository onboardingRepository;

    @Override
    public Onboarding updateOnboarding(Onboarding onboarding) throws CatleanException {
        try {
            return entityToDomain(onboardingRepository.save(domainToEntity(onboarding)));
        } catch (Exception e) {
            LOGGER.error("Failed to update onboarding {}", onboarding);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to update onboarding + " + onboarding.getId())
                    .build();
        }
    }
}
