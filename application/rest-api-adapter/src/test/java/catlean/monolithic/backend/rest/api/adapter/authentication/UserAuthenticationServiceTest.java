package catlean.monolithic.backend.rest.api.adapter.authentication;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
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
    void should_authenticate_and_get_user() throws CatleanException {
        // Given
        final UserFacadeAdapter userFacadeAdapter = mock(UserFacadeAdapter.class);
        final AuthenticationService userAuthenticationService = new AuthenticationService(userFacadeAdapter);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(authentication.getDetails()).thenReturn(decodedJWT);
        final Map<String, Claim> claims = mock(Map.class);
        when(decodedJWT.getClaims()).thenReturn(claims);
        when(claims.containsKey("https://catlean.fr/email")).thenReturn(true);
        final Claim claim = mock(Claim.class);
        when(claims.get("https://catlean.fr/email")).thenReturn(claim);
        final String mail = faker.ancient().god();
        when(claim.asString()).thenReturn(mail);
        SecurityContextHolder.setContext(securityContext);
        when(userFacadeAdapter.getOrCreateUserFromMail(mail)).thenReturn(User.builder().mail(mail).build());

        // When
        final User authenticatedUser = userAuthenticationService.getAuthenticatedUser();

        // Then
        assertThat(authenticatedUser.getMail()).isEqualTo(mail);
    }

    @Test
    void should_raise_an_exception_when_missing_mail_in_jwt_token() {
        // Given
        final UserFacadeAdapter userFacadeAdapter = mock(UserFacadeAdapter.class);
        final AuthenticationService userAuthenticationService = new AuthenticationService(userFacadeAdapter);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(authentication.getDetails()).thenReturn(decodedJWT);
        final Map<String, Claim> claims = mock(Map.class);
        when(decodedJWT.getClaims()).thenReturn(claims);
        when(claims.containsKey("https://catlean.fr/email")).thenReturn(false);

        // When
        CatleanException exception = null;
        try {
            userAuthenticationService.getAuthenticatedUser();
        } catch (CatleanException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Mail not found in auth0 JWT token");
        assertThat(exception.getCode()).isEqualTo("T.MISSING_MAIL_AUTH0");

    }
}
