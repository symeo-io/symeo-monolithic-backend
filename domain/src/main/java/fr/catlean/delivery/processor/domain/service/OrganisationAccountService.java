package fr.catlean.delivery.processor.domain.service;

import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.port.out.OrganisationAccountAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganisationAccountService {
    final OrganisationAccountAdapter organisationAccountAdapter;

    public OrganisationAccount getOrganisationForName(String organisationName) {
        return organisationAccountAdapter.findOrganisationForName(organisationName);
    }
}
