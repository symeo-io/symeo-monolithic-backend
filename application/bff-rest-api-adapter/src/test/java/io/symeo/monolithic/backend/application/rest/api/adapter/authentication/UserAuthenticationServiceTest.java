package io.symeo.monolithic.backend.application.rest.api.adapter.authentication;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.port.in.UserFacadeAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserAuthenticationServiceTest {

    private final Faker faker = new Faker();


    @Test
    void should_authenticate_and_get_user() throws SymeoException {
        // Given
        final UserFacadeAdapter userFacadeAdapter = mock(UserFacadeAdapter.class);
        final AuthenticationService userAuthenticationService = new AuthenticationService(userFacadeAdapter,
                new Auth0ContextProvider());
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(authentication.getDetails()).thenReturn(decodedJWT);
        final Map<String, Claim> claims = mock(Map.class);
        when(decodedJWT.getClaims()).thenReturn(claims);
        when(claims.containsKey("https://symeo.io/email")).thenReturn(true);
        final Claim claim = mock(Claim.class);
        when(claims.get("https://symeo.io/email")).thenReturn(claim);
        final String mail = faker.ancient().god();
        when(claim.asString()).thenReturn(mail);
        SecurityContextHolder.setContext(securityContext);
        when(userFacadeAdapter.getOrCreateUserFromEmail(mail)).thenReturn(User.builder().email(mail).build());

        // When
        final User authenticatedUser = userAuthenticationService.getAuthenticatedUser();

        // Then
        assertThat(authenticatedUser.getEmail()).isEqualTo(mail);
    }

    @Test
    void should_raise_an_exception_when_missing_mail_in_jwt_token() {
        // Given
        final UserFacadeAdapter userFacadeAdapter = mock(UserFacadeAdapter.class);
        final AuthenticationService userAuthenticationService = new AuthenticationService(userFacadeAdapter,
                new Auth0ContextProvider());
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(authentication.getDetails()).thenReturn(decodedJWT);
        final Map<String, Claim> claims = mock(Map.class);
        when(decodedJWT.getClaims()).thenReturn(claims);
        when(claims.containsKey("https://symeo.io/email")).thenReturn(false);

        // When
        SymeoException exception = null;
        try {
            userAuthenticationService.getAuthenticatedUser();
        } catch (SymeoException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Mail not found in auth0 JWT token");
        assertThat(exception.getCode()).isEqualTo("T.MISSING_MAIL_AUTH0");

    }
}
