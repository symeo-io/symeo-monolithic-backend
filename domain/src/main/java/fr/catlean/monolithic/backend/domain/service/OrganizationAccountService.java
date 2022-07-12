package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationAccountAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationAccountService {
    final OrganizationAccountAdapter organizationAccountAdapter;

    public OrganizationAccount getOrganizationForName(String organizationName) throws CatleanException {
        return organizationAccountAdapter.findOrganizationForName(organizationName);
    }
}
