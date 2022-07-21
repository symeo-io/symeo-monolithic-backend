package catlean.monolithic.backend.rest.api.adapter.authentication;

import com.auth0.jwt.interfaces.Claim;

import java.util.Map;

public interface AuthenticationContextProvider {
    Map<String, Claim> getClaims();
}
