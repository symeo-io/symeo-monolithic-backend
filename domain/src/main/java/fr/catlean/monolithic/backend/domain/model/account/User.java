package fr.catlean.monolithic.backend.domain.model.account;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class User {
    public static final String PENDING = "PENDING";
    public static final String ACTIVE = "ACTIVE";
    String email;
    UUID id;
    Organization organization;
    @Builder.Default
    Onboarding onboarding = Onboarding.builder().id(UUID.randomUUID()).build();
    @Builder.Default
    String status = PENDING;

    public void hasConnectedToVcs() {
        this.onboarding = this.onboarding.toBuilder().hasConnectedToVcs(true).build();
    }

    public void hasConfiguredTeam() {
        this.onboarding = this.onboarding.toBuilder().hasConfiguredTeam(true).build();
    }

    public User isActive() {
        return this.toBuilder().status(ACTIVE).build();
    }
}
