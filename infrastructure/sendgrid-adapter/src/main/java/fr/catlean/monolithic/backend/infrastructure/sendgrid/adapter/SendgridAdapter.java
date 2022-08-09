package fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.EmailDeliveryAdapter;
import fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter.client.SendgridApiClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class SendgridAdapter implements EmailDeliveryAdapter {

    private final SendgridApiClient sendgridApiClient;

    @Override
    public void sendInvitationForUsers(List<User> createdUsers) {
        LOGGER.info("Sending invitation email(s) for user(s) : {}", createdUsers);
        for (User createdUser : createdUsers) {
            String email = createdUser.getEmail();
            try {
                sendgridApiClient.sendInvitationEmail(email);
            } catch (CatleanException e) {
                LOGGER.error("Invitation email not send to email {}", email);
            }
        }
    }
}
