package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;

public interface OrganizationFacadeAdapter {

    Organization createOrganizationForVcsNameAndExternalId(final String vcsOrganizationName, final String externalId) throws CatleanException;
}
