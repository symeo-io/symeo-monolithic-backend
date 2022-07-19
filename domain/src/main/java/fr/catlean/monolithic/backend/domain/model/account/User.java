package fr.catlean.monolithic.backend.domain.model.account;

import fr.catlean.monolithic.backend.domain.model.Onboarding;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class User {
    String mail;
    UUID id;
    Organization organization;
    @Builder.Default
    Onboarding onboarding = Onboarding.builder().build();

    public void hasConnectedToVcs() {
        this.onboarding = this.onboarding.toBuilder().hasConnectedToVcs(true).build();
    }
}
