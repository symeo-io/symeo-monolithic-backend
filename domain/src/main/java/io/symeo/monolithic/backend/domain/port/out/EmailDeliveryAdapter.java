package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.User;

import java.util.List;

public interface EmailDeliveryAdapter {
    void sendInvitationForUsers(Organization organization, User fromUser, List<User> createdUsers);
}
