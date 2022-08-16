package fr.catlean.monolithic.backend.application.rest.api.adapter.authentication;

import com.auth0.jwt.interfaces.Claim;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.MISSING_MAIL_AUTH0;

@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private UserFacadeAdapter userFacadeAdapter;
    private AuthenticationContextProvider authenticationContextProvider;
    private final static String CATLEAN_MAIL_KEY = "https://catlean.fr/email";

    public User getAuthenticatedUser() throws CatleanException {
        final Map<String, Claim> claims = authenticationContextProvider.getClaims();
        if (!claims.containsKey(CATLEAN_MAIL_KEY)) {
            final String message = "Mail not found in auth0 JWT token";
            LOGGER.error(message);
            throw CatleanException.builder()
                    .code(MISSING_MAIL_AUTH0)
                    .message(message)
                    .build();
        }
        final String mail = claims.get(CATLEAN_MAIL_KEY).asString();
        return userFacadeAdapter.getOrCreateUserFromEmail(mail);
    }
}
