package catlean.monolithic.backend.rest.api.adapter.authentication;

import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.UserQueryAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserAuthenticationServiceTest {
//
//    @Test
//    void should_authenticate_and_get_user() {
//        // Given
//        final UserQueryAdapter userQueryAdapter = mock(UserQueryAdapter.class);
//        final UserAuthenticationService userAuthenticationService = new UserAuthenticationService(userQueryAdapter);
//        Authentication authentication = Mockito.mock(Authentication.class);
//        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
//
//        // When
//        final User authenticatedUser = userAuthenticationService.getAuthenticatedUser();
//
//
//        // Then
//    }
}
