package io.symeo.monolithic.backend.domain.model.account;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Data
@Builder(toBuilder = true)
public class User {
    public static final String PENDING = "PENDING";
    public static final String ACTIVE = "ACTIVE";
    String email;
    UUID id;
    List<Organization> organizations;
    @Builder.Default
    Onboarding onboarding = Onboarding.builder().id(UUID.randomUUID()).build();
    @Builder.Default
    String status = PENDING;

    public Organization getOrganization() {
        return isNull(this.organizations) ? null : this.organizations.get(0);
    }

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
