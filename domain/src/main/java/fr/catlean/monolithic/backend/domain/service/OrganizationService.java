package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationService implements OrganizationFacadeAdapter {
    final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;

    public Organization getOrganizationForName(String organizationName) throws CatleanException {
        return accountOrganizationStorageAdapter.findOrganizationForName(organizationName);
    }

    @Override
    public Organization createOrganizationForVcsNameAndExternalId(String vcsOrganizationName, String externalId) throws CatleanException {
        return accountOrganizationStorageAdapter.createOrganization(
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
