package fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter.client;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode;
import fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter.properties.SendgridProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class SendgridApiClient {

    private final SendGrid sendGrid;
    private final Email catleanEmail;
    private final SendgridProperties sendgridProperties;

    public SendgridApiClient(SendgridProperties sendgridProperties) {
        this.sendgridProperties = sendgridProperties;
        this.sendGrid = new SendGrid(sendgridProperties.getApiKey());
        this.catleanEmail = new Email(sendgridProperties.getCatleanEmail());
    }


    public void sendInvitationEmail(final String email) throws CatleanException {
        final Mail mail = new Mail(catleanEmail, sendgridProperties.getInvitationSubject(), new Email(email),
                new Content());
        mail.getPersonalization().get(0).addSubstitution(sendgridProperties.getInvitationEmailPlaceholder(), email);
        mail.setTemplateId(sendgridProperties.getInvitationTemplateId());
        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            LOGGER.info("Email sent to Sendgrid returned a status code equals to {}", response.getStatusCode());
        } catch (IOException ex) {
            final String message = String.format("Failed to send mail %s to email %s", mail, email);
            LOGGER.error(message, ex);
            throw CatleanException.builder()
                    .code(CatleanExceptionCode.SENDGRID_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
