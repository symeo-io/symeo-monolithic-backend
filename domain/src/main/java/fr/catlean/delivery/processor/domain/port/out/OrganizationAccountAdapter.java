package fr.catlean.delivery.processor.domain.port.out;

import fr.catlean.delivery.processor.domain.model.account.OrganizationAccount;

public interface OrganizationAccountAdapter {
    OrganizationAccount findOrganizationForName(String organizationName);
}
