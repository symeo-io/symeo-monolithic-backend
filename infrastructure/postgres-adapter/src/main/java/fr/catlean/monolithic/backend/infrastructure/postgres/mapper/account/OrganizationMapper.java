package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account;

import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;

import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;

public interface OrganizationMapper {

    static OrganizationEntity domainToEntity(final Organization organization) {
        return OrganizationEntity.builder()
                .id(isNull(organization.getId()) ? UUID.randomUUID().toString() : organization.getId().toString())
                .name(organization.getName())
                .externalId(organization.getExternalId())
                .build();
    }


    static Organization entityToDomain(final OrganizationEntity organization) {
        return Organization.builder()
                .id(UUID.fromString(organization.getId()))
                .vcsConfiguration(
                        VcsConfiguration.builder().organizationName(organization.getName()).build()
                )
                .name(organization.getName())
                .externalId(organization.getExternalId())
                .build();
    }
}
