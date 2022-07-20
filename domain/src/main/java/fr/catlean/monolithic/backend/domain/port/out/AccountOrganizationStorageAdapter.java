package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;

public interface AccountOrganizationStorageAdapter {
    Organization findVcsOrganizationForName(String vcsOrganizationName) throws CatleanException;

    Organization createOrganization(Organization organization) throws CatleanException;
}
