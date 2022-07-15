package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;

public interface OrganizationStorageAdapter {
    Organization findOrganizationForName(String organizationName) throws CatleanException;

    Organization createOrganization(Organization organization) throws CatleanException;

}
