package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.model.account.User;

import java.util.Optional;

public interface AccountStorageAdapter {
    Optional<User> getUserFromMail(String mail);

    User createUserWithMail(String mail);
}
