package io.symeo.monolithic.backend.infrastructure.postgres.repository.account;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OnboardingRepository extends JpaRepository<OnboardingEntity, UUID> {
}
