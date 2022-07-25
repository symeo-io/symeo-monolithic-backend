package fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter;

import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.EmailDeliveryAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SendgridAdapter implements EmailDeliveryAdapter {

    @Override
    public void sendInvitationForUsers(List<User> createdUsers) {
        LOGGER.info("Sending invitation email(s) for user(s) : {}", createdUsers);
    }
}
