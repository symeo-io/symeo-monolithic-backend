package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.model.account.User;

public interface UserFacadeAdapter {

    User getOrCreateUserFromMail(String mail);
}
