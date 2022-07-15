package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.AccountStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserService implements UserFacadeAdapter {

    private final AccountStorageAdapter accountStorageAdapter;

    @Override
    public User getOrCreateUserFromMail(String mail) {
        return accountStorageAdapter.getUserFromMail(mail).orElseGet(() -> accountStorageAdapter.createUserWithMail(mail));
    }

}
