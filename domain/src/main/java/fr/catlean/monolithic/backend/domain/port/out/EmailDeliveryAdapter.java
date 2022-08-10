package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.List;

public interface EmailDeliveryAdapter {
    void sendInvitationForUsers(Organization organization, User fromUser, List<User> createdUsers);
}
