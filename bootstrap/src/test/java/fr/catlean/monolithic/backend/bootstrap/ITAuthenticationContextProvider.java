package fr.catlean.monolithic.backend.bootstrap;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationContextProvider;
import com.auth0.jwt.interfaces.Claim;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class ITAuthenticationContextProvider implements AuthenticationContextProvider {

    private final Map<String, Claim> claimMap = new HashMap<>();

    public void authorizeUserForMail(final String mail) {
        final Claim claim = Mockito.mock(Claim.class);
        when(claim.asString()).thenReturn(mail);
        claimMap.put("https://catlean.fr/email", claim);
    }

    @Override
    public Map<String, Claim> getClaims() {
        return this.claimMap;
    }
}