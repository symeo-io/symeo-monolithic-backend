package fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter.properties;

import lombok.Data;

@Data
public class SendgridProperties {
    private String apiKey;
    private String catleanEmail;
    private String invitationTemplateId;
    private String invitationSubject;
    private String invitationEmailPlaceholder;
}
