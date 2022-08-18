package io.symeo.monolithic.backend.infrastructure.sendgrid.adapter.client;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.infrastructure.sendgrid.adapter.properties.SendgridProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class SendgridApiClient {

    private final SendGrid sendGrid;
    private final SendgridProperties sendgridProperties;

    public SendgridApiClient(SendgridProperties sendgridProperties) {
        this.sendgridProperties = sendgridProperties;
        this.sendGrid = new SendGrid(sendgridProperties.getApiKey());
    }


    public void sendInvitationEmail(final String organizationName, final String fromUserEmail, final String email) throws SymeoException {
        final Email to = new Email(email);
        final Mail mail = new Mail();
        mail.setFrom(new Email(sendgridProperties.getSymeoEmail(), "Symeo Support"));
        final Personalization personalization = new Personalization();
        personalization.addDynamicTemplateData(sendgridProperties.getInvitationFromUserEmailPlaceholder(),
                fromUserEmail);
        personalization.addDynamicTemplateData(sendgridProperties.getInvitationOrganizationNamePlaceholder(),
                organizationName);
        personalization.addTo(to);
        mail.addPersonalization(personalization);
        mail.setTemplateId(sendgridProperties.getInvitationTemplateId());
        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            LOGGER.info("Email sent to Sendgrid returned a status code equals to {}", response.getStatusCode());
            if (response.getStatusCode() > 299) {
                LOGGER.error("Error while sending invitation email : {}", response.getBody());
            }
        } catch (IOException ex) {
            final String message = String.format("Failed to send mail %s to email %s", mail, email);
            LOGGER.error(message, ex);
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.SENDGRID_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
