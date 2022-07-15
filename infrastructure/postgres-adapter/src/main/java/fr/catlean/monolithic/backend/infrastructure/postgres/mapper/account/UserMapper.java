package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account;

import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;

import java.util.UUID;

public interface UserMapper {

    static User entityToDomain(final UserEntity userEntity) {
        return User.builder()
                .id(UUID.fromString(userEntity.getId()))
                .mail(userEntity.getMail())
                .build();

    }
}
