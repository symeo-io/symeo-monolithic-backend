package io.symeo.monolithic.backend.infrastructure.sendgrid.adapter.properties;

import lombok.Data;

@Data
public class SendgridProperties {
    private String apiKey;
    private String symeoEmail;
    private String invitationTemplateId;
    private String invitationFromUserEmailPlaceholder;
    private String invitationOrganizationNamePlaceholder;
}
