package io.symeo.monolithic.backend.infrastructure.sendgrid.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.sendgrid.adapter.client.SendgridApiClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SendgridAdapterTest {

    private final Faker faker = new Faker();

    @Test
    void should_send_mails_given_users() throws SymeoException {
        // Given
        final SendgridApiClient sendgridApiClient = Mockito.mock(SendgridApiClient.class);
        final SendgridAdapter sendgridAdapter = new SendgridAdapter(sendgridApiClient);
        final Organization organization = Organization.builder().name(faker.ancient().god())
                .id(UUID.randomUUID())
                .vcsOrganization(Organization.VcsOrganization.builder().build().builder().build())
                .build();
        final User fromUser = User.builder()
                .email(faker.harryPotter().character())
                .build();
        final List<User> users = List.of(
                User.builder().email(faker.cat().name()).build(),
                User.builder().email(faker.name().name()).build(),
                User.builder().email(faker.harryPotter().character()).build()
        );

        // When
        sendgridAdapter.sendInvitationForUsers(organization, fromUser,
                users
        );

        // Then
        final ArgumentCaptor<String> emailArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> fromUserArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> organizationNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(sendgridApiClient, Mockito.times(3))
                .sendInvitationEmail(organizationNameArgumentCaptor.capture(), fromUserArgumentCaptor.capture(),
                        emailArgumentCaptor.capture());
        organizationNameArgumentCaptor.getAllValues()
                .forEach(organizationName -> assertThat(organizationName).isEqualTo(organization.getName()));
        fromUserArgumentCaptor.getAllValues()
                .forEach(userMail -> assertThat(userMail).isEqualTo(fromUser.getEmail()));
        assertThat(emailArgumentCaptor.getAllValues()).isEqualTo(users.stream().map(User::getEmail).toList());
    }

    @Test
    void should_not_interrupt_invitation_when_sendgrid_return_an_exception() throws SymeoException {
        // Given
        final SendgridApiClient sendgridApiClient = Mockito.mock(SendgridApiClient.class);
        final SendgridAdapter sendgridAdapter = new SendgridAdapter(sendgridApiClient);
        final List<User> users = List.of(
                User.builder().email(faker.cat().name()).build(),
                User.builder().email(faker.name().name()).build(),
                User.builder().email(faker.harryPotter().character()).build()
        );
        final Organization organization = Organization.builder().name(faker.ancient().god())
                .id(UUID.randomUUID())
                .vcsOrganization(Organization.VcsOrganization.builder().build())
                .build();
        final User fromUser = User.builder()
                .email(faker.harryPotter().character())
                .build();

        // When
        doThrow(SymeoException.class)
                .when(sendgridApiClient)
                .sendInvitationEmail(organization.getName(), fromUser.getEmail(), users.get(0).getEmail());
        doThrow(SymeoException.class)
                .when(sendgridApiClient)
                .sendInvitationEmail(organization.getName(), fromUser.getEmail(), users.get(1).getEmail());
        sendgridAdapter.sendInvitationForUsers(
                organization, fromUser, users
        );

        // Then
        verify(sendgridApiClient, times(3)).sendInvitationEmail(anyString(), anyString(), anyString());
    }
}
