package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.User;

import java.util.List;

public interface EmailDeliveryAdapter {
    void sendInvitationForUsers(Organization organization, User fromUser, List<User> createdUsers);
}
