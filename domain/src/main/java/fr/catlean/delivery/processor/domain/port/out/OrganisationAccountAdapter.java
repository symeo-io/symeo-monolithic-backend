package fr.catlean.delivery.processor.domain.port.out;

import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;

public interface OrganisationAccountAdapter {
    OrganisationAccount findOrganisationForName(String organisationName);
}
