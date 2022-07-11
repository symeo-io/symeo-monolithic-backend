package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;

public interface OrganizationAccountAdapter {
    OrganizationAccount findOrganizationForName(String organizationName) throws CatleanException;
}
