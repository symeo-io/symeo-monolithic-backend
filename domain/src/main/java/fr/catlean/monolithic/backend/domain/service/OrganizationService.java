package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationService implements OrganizationFacadeAdapter {
    final OrganizationStorageAdapter organizationStorageAdapter;

    public Organization getOrganizationForName(String organizationName) throws CatleanException {
        return organizationStorageAdapter.findOrganizationForName(organizationName);
    }

    @Override
    public Organization createOrganizationForVcsNameAndExternalId(String vcsOrganizationName, String externalId) throws CatleanException {
        return organizationStorageAdapter.createOrganization(
                Organization.builder()
                        .name(vcsOrganizationName)
                        .externalId(externalId)
                        .vcsConfiguration(
                                VcsConfiguration.builder()
                                        .organizationName(vcsOrganizationName)
                                        .build()
                        )
                        .build()
        );
    }


}
