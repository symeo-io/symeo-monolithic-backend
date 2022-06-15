package fr.catlean.delivery.processor.domain.service;

import fr.catlean.delivery.processor.domain.model.account.OrganizationAccount;
import fr.catlean.delivery.processor.domain.port.out.OrganizationAccountAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationAccountService {
    final OrganizationAccountAdapter organizationAccountAdapter;

    public OrganizationAccount getOrganizationForName(String organizationName) {
        return organizationAccountAdapter.findOrganizationForName(organizationName);
    }
}
