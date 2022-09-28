package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Onboarding;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OnboardingMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OnboardingRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.UUID;

public class PostgresOnboardingAdapterTestIT extends AbstractPostgresIT {

    private final Faker faker = new Faker();
    @Autowired
    private OnboardingRepository onboardingRepository;

    @AfterEach
    void tearDown() {
        onboardingRepository.deleteAll();
    }

    @Test
    void should_update_onboarding() throws SymeoException {
        // Given
        final PostgresOnboardingAdapter postgresAccountOnboardingAdapter =
                new PostgresOnboardingAdapter(onboardingRepository);
        Onboarding onboarding = Onboarding.builder().build();
        onboardingRepository.save(OnboardingMapper.domainToEntity(onboarding));
        onboarding =
                onboarding.toBuilder().id(UUID.randomUUID()).hasConnectedToVcs(true).hasConfiguredTeam(true).build();

        // When
        final Onboarding updateOnboarding = postgresAccountOnboardingAdapter.updateOnboarding(onboarding);

        // Then
        Assertions.assertThat(updateOnboarding.getId()).isEqualTo(onboarding.getId());
        Assertions.assertThat(updateOnboarding.getHasConnectedToVcs()).isEqualTo(onboarding.getHasConnectedToVcs());
        Assertions.assertThat(updateOnboarding.getHasConfiguredTeam()).isEqualTo(onboarding.getHasConfiguredTeam());
    }
}
