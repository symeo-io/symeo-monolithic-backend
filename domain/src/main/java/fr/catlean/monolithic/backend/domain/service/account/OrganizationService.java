package fr.catlean.monolithic.backend.domain.service.account;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
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
                        .vcsOrganization(
                                VcsOrganization.builder()
                                        .externalId(externalId)
                                        .name(vcsOrganizationName)
                                        .build()
                        )
                        .build()
        );
    }


}
