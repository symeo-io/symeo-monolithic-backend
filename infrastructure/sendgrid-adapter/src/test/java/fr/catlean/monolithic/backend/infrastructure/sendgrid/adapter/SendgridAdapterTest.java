package fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter.client.SendgridApiClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SendgridAdapterTest {

    private final Faker faker = new Faker();

    @Test
    void should_send_mails_given_users() throws CatleanException {
        // Given
        final SendgridApiClient sendgridApiClient = Mockito.mock(SendgridApiClient.class);
        final SendgridAdapter sendgridAdapter = new SendgridAdapter(sendgridApiClient);

        // When
        sendgridAdapter.sendInvitationForUsers(
                List.of(
                        User.builder().email(faker.cat().name()).build(),
                        User.builder().email(faker.name().name()).build(),
                        User.builder().email(faker.harryPotter().character()).build()
                )
        );

        // Then
        final ArgumentCaptor<String> emailArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendgridApiClient, Mockito.times(3))
                .sendInvitationEmail(emailArgumentCaptor.capture());
    }

    @Test
    void should_not_interrupt_invitation_when_sendgrid_return_an_exception() throws CatleanException {
        // Given
        final SendgridApiClient sendgridApiClient = Mockito.mock(SendgridApiClient.class);
        final SendgridAdapter sendgridAdapter = new SendgridAdapter(sendgridApiClient);
        final List<User> users = List.of(
                User.builder().email(faker.cat().name()).build(),
                User.builder().email(faker.name().name()).build(),
                User.builder().email(faker.harryPotter().character()).build()
        );

        // When
        doThrow(CatleanException.class)
                .when(sendgridApiClient)
                .sendInvitationEmail(users.get(0).getEmail());
        doThrow(CatleanException.class)
                .when(sendgridApiClient)
                .sendInvitationEmail(users.get(1).getEmail());
        sendgridAdapter.sendInvitationForUsers(
                users
        );

        // Then
        verify(sendgridApiClient, times(3)).sendInvitationEmail(anyString());
    }
}
