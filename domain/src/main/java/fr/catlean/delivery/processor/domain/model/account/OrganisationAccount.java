package fr.catlean.delivery.processor.domain.model.account;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrganisationAccount {
    String name;
    VcsConfiguration vcsConfiguration;
}
