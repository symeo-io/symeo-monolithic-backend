package io.symeo.monolithic.backend.domain.bff.model.account;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder
@Value
public class OrganizationApiKey {
    UUID id;
    UUID organizationId;
    String name;
    String key;
}
