package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.List;

public interface EmailDeliveryAdapter {
    void sendInvitationForUsers(List<User> createdUsers);
}
