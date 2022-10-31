package io.symeo.monolithic.backend.infrastructure.sendgrid.adapter;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.port.out.EmailDeliveryAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.sendgrid.adapter.client.SendgridApiClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class SendgridAdapter implements EmailDeliveryAdapter {

    private final SendgridApiClient sendgridApiClient;

    @Override
    public void sendInvitationForUsers(Organization organization, User fromUser, List<User> createdUsers) {
        LOGGER.info("Sending invitation email(s) for user(s) : {}", createdUsers);
        for (User createdUser : createdUsers) {
            String email = createdUser.getEmail();
            try {
                sendgridApiClient.sendInvitationEmail(organization.getName(), fromUser.getEmail(), email);
            } catch (SymeoException e) {
                LOGGER.error("Invitation email not send to email {}", email);
            }
        }
    }

}
