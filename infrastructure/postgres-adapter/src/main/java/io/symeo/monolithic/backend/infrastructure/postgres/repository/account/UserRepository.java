package io.symeo.monolithic.backend.infrastructure.postgres.repository.account;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmail(String email);

    @Query(value = "select * from organization_storage.user where id in " +
            "( select user_id from organization_storage.user_to_organization where organization_id = :organizationId )",
            nativeQuery = true)
    List<UserEntity> findAllForOrganizationId(@Param("organizationId") UUID organizationId);

    List<UserEntity> findAllByEmailIn(List<String> emails);
}
