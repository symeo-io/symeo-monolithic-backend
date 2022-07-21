package fr.catlean.monolithic.backend.bootstrap.it;

import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;

@ActiveProfiles({"it"})
public class CatleanUserOnboardingIT extends AbstractCatleanMonolithicBackendIT{

    @Autowired
    public OrganizationRepository organizationRepository;

    @Test
    public void first_test() {
        client.post().uri(URI.create("http://www.google.fr")).exchange();
    }
}
