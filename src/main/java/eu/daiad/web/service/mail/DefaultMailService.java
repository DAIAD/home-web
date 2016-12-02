package eu.daiad.web.service.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.MailErrorCode;

/**
 * A simple implementation of interface @{link IMailService} using Spring Boot
 * mail module.
 */
@Service
public class DefaultMailService implements IMailService {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DefaultMailService.class);

    /**
     * Name of the logger for send mail operations.
     */
    protected static final String LOGGER_SEND_MAIL = "SendMailLogger";

    /**
     * Logger for logging send mail operations
     */
    protected static final Log sendMailLogger = LogFactory.getLog(LOGGER_SEND_MAIL);

    /**
     * Message source for localizing error messages.
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * True if mail system is enabled; Otherwise False
     */
    @Value("${daiad.mail.enabled}")
    private boolean isMailSystemEnabled;

    /**
     * Sender default address
     */
    @Value("${daiad.mail.sender.address}")
    private String mailSenderDefaultAddress;

    /**
     * Sender default name
     */
    @Value("${daiad.mail.sender.name}")
    private String mailSenderDefaultName;

    /**
     * An instance of @{link JavaMailSenderImpl} as configured by Spring Boot
     * auto-configuration.
     */
    @Autowired
    private JavaMailSenderImpl mailSender;

    /**
     * An instance of @{link SpringTemplateEngine} as configured in @{link
     * MailConfig}.
     */
    @Autowired
    private SpringTemplateEngine templateEngine;

    /**
     * Sends a mail. The mail content is generated from a template. Data is
     * provided using an object. The content of the mail is localized.
     *
     * @param message the message to be sent
     */
    @Override
    public void send(Message message) {
        if (!isMailSystemEnabled) {
            logger.warn(String.format("Send mail request to recipient [%s] has failed. Mail system is disabled.",
                            StringUtils.join(message.getRecipients(), ',')));

            return;
        }

        try {
            // Set default sender
            if (message.getSender() == null) {
                message.setSender(mailSenderDefaultAddress, mailSenderDefaultName);
            }

            MimeMessage mimeMessage = createMimeMessage(message);

            logMessage(mimeMessage);

            mailSender.send(mimeMessage);
        } catch (ApplicationException appEx) {
            throw appEx;
        } catch (Exception ex) {
            MailErrorCode code = MailErrorCode.SENT_FAILED;

            String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

            throw ApplicationException.wrap(ex, code, pattern);
        }
    }

    /**
     * Creates a new mail message given a template and a data model.
     *
     * @param message the message to send.
     * @return the new @{link MimeMessage}
     *
     * @throws MessagingException if message creation fails.
     */
    private MimeMessage createMimeMessage(Message message) throws MessagingException, IOException {
        final MimeMessage mimeMessage = mailSender.createMimeMessage();

        final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        mimeMessageHelper.setSubject(message.getSubject());

        if (StringUtils.isBlank(message.getSender().getName())) {
            mimeMessageHelper.setFrom(message.getSender().getAddress());
        } else {
            mimeMessageHelper.setFrom(message.getSender().getAddress(), message.getSender().getName());
        }

        for (EmailAddress recipient : message.getRecipients()) {
            if (StringUtils.isBlank(recipient.getName())) {
                mimeMessageHelper.addTo(recipient.getAddress());
            } else {
                mimeMessageHelper.addTo(recipient.getAddress(), recipient.getName());
            }
        }

        String htmlContent = renderTemplate(message.getLocale(), message.getTemplate(), message.getModel());
        mimeMessageHelper.setText(htmlContent, true);

        return mimeMessage;
    }

    /**
     * Renders the HTML body of a mail using a template. When constructing the
     * name of the template, the locale is appended to {@code template},
     * separated by a comma (,).
     *
     * @param locale the locale of the recipient.
     * @param template the template name.
     * @param model the model passed to the view during the rendering process.
     * @return the rendered text.
     */
    private String renderTemplate(String locale, String template, Object model) {
        if(StringUtils.isBlank(locale)) {
            locale = Locale.ENGLISH.toString();
        }

        final Context ctx = new Context(new Locale(locale));

        ctx.setVariable("model", model);

        template = template + "-" + locale;

        return templateEngine.process(template, ctx);
    }

    /**
     * Logs an email message.
     *
     * @param message the message to log.
     */
    private void logMessage(MimeMessage message) {
        try {
            List<String> recipients = new ArrayList<String>();
            for (Address address : message.getAllRecipients()) {
                recipients.add(address.toString());
            }

            String text = String.format("Mail sent to [%s] with subject [%s].",StringUtils.join(recipients,  ","), message.getSubject());

            sendMailLogger.info(text);
        } catch(MessagingException ex) {
            MailErrorCode code = MailErrorCode.LOG_FAILED;

            String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

            throw ApplicationException.wrap(ex, code, pattern);
        }

    }

}
