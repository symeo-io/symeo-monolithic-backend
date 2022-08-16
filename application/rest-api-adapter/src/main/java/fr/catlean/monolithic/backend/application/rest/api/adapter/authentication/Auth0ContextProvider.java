package fr.catlean.monolithic.backend.application.rest.api.adapter.authentication;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class Auth0ContextProvider implements AuthenticationContextProvider {

    @Override
    public Map<String, Claim> getClaims() {
        return ((DecodedJWT) SecurityContextHolder.getContext().getAuthentication().getDetails()).getClaims();
    }
}
