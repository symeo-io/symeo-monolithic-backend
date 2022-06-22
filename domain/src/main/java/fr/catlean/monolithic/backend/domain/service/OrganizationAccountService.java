package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationAccountAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationAccountService {
    final OrganizationAccountAdapter organizationAccountAdapter;

    public OrganizationAccount getOrganizationForName(String organizationName) {
        return organizationAccountAdapter.findOrganizationForName(organizationName);
    }
}
