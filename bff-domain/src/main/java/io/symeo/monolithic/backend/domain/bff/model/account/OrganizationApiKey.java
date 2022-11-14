package io.symeo.monolithic.backend.domain.bff.model.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

@Builder
@Value
@AllArgsConstructor
public class OrganizationApiKey {
    UUID id;
    UUID organizationId;
    String name;
    String key;

    public OrganizationApiKey(UUID organizationId, String name) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.name = name;
        this.key = OrganizationApiKey.generateRandomApiKey();
    }

    public String getHiddenKey() {
        return "************" + this.key.substring(this.key.length() - 4);
    }

    private static String generateRandomApiKey() {
        return RandomStringUtils.randomAlphanumeric(48);
    }
}
