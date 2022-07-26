package fr.catlean.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Onboarding;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresAccountOnboardingAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OnboardingMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OnboardingRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresAccountOnboardingAdapterTestIT {

    private final Faker faker = new Faker();
    @Autowired
    private OnboardingRepository onboardingRepository;

    @AfterEach
    void tearDown() {
        onboardingRepository.deleteAll();
    }

    @Test
    void should_update_onboarding() throws CatleanException {
        // Given
        final PostgresAccountOnboardingAdapter postgresAccountOnboardingAdapter =
                new PostgresAccountOnboardingAdapter(onboardingRepository);
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
