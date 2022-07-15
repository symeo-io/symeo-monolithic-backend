package fr.catlean.monolithic.backend.domain.model.account;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class User {
    String mail;
    UUID id;
    OrganizationAccount organizationAccount;
}
