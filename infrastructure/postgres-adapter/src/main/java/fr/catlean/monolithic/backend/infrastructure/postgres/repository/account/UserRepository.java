package fr.catlean.monolithic.backend.infrastructure.postgres.repository.account;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findAllByOrganizationId(String organizationId);
}
