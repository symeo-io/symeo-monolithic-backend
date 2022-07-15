package catlean.monolithic.backend.rest.api.adapter.authentication;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private UserFacadeAdapter userFacadeAdapter;
    private final static String CATLEAN_MAIL_KEY = "https://catlean.fr/email";

    public User getAuthenticatedUser() throws CatleanException {
        final Map<String, Claim> claims =
                ((DecodedJWT) SecurityContextHolder.getContext().getAuthentication().getDetails()).getClaims();
        if (!claims.containsKey(CATLEAN_MAIL_KEY)) {
            final String message = "Mail not found in auth0 JWT token";
            LOGGER.error(message);
            throw CatleanException.builder()
                    .code("T.MISSING_MAIL_AUTH0")
                    .message(message)
                    .build();
        }
        final String mail = claims.get(CATLEAN_MAIL_KEY).asString();
        return userFacadeAdapter.getOrCreateUserFromMail(mail);
    }
}
