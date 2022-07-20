package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account;

import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface OrganizationMapper {

    static OrganizationEntity domainToEntity(final Organization organization) {
        return OrganizationEntity.builder()
                .id(isNull(organization.getId()) ? UUID.randomUUID().toString() : organization.getId().toString())
                .name(organization.getName())
                .build();
    }

    static Organization entityToDomain(final OrganizationEntity organizationEntity) {
        return Organization.builder()
                .name(organizationEntity.getName())
                .id(UUID.fromString(organizationEntity.getId()))
                .build();

    }


    static Organization entityToDomain(final VcsOrganizationEntity vcsOrganizationEntity) {
        final OrganizationEntity organizationEntity = vcsOrganizationEntity.getOrganizationEntity();
        return Organization.builder()
                .id(UUID.fromString(organizationEntity.getId()))
                .name(organizationEntity.getName())
                .vcsOrganization(vcsEntityToDomain(vcsOrganizationEntity))
                .build();
    }

    static VcsOrganizationEntity vcsDomainToEntity(final Organization organization) {
        final VcsOrganization vcsOrganization = organization.getVcsOrganization();
        return VcsOrganizationEntity.builder()
                .organizationEntity(domainToEntity(organization))
                .vcsId(vcsOrganization.getVcsId())
                .name(vcsOrganization.getName())
                .externalId(vcsOrganization.getExternalId())
                .build();
    }

    private static VcsOrganization vcsEntityToDomain(final VcsOrganizationEntity vcsOrganizationEntity) {
        return VcsOrganization.builder()
                .vcsId(vcsOrganizationEntity.getVcsId())
                .id(vcsOrganizationEntity.getId().toString())
                .name(vcsOrganizationEntity.getName())
                .externalId(vcsOrganizationEntity.getExternalId())
                .build();
    }

}
