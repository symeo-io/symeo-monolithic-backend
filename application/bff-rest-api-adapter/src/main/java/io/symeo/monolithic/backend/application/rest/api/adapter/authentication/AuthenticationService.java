package io.symeo.monolithic.backend.application.rest.api.adapter.authentication;

import com.auth0.jwt.interfaces.Claim;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.port.in.UserFacadeAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.MISSING_MAIL_AUTH0;

@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private UserFacadeAdapter userFacadeAdapter;
    private AuthenticationContextProvider authenticationContextProvider;
    private final static String SYMEO_MAIL_KEY = "https://symeo.io/email";

    public User getAuthenticatedUser() throws SymeoException {
        final Map<String, Claim> claims = authenticationContextProvider.getClaims();
        if (!claims.containsKey(SYMEO_MAIL_KEY)) {
            final String message = "Mail not found in auth0 JWT token";
            LOGGER.error(message);
            throw SymeoException.builder()
                    .code(MISSING_MAIL_AUTH0)
                    .message(message)
                    .build();
        }
        final String mail = claims.get(SYMEO_MAIL_KEY).asString();
        return userFacadeAdapter.getOrCreateUserFromEmail(mail);
    }
}
