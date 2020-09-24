package eu.daiad.common.service.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.MailErrorCode;

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
    private static final String LOGGER_SEND_MAIL = "SEND_MAIL";
    
    /**
     * Logger for logging send mail operations
     */
    private static final Log sendMailLogger = LogFactory.getLog(LOGGER_SEND_MAIL);
    
    
    /**
     * Message source for localizing error messages.
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * True if mail system is enabled; Otherwise False
     */
    @Value("${daiad.mail.enabled:false}")
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
    private JavaMailSender mailSender;

    /**
     * An instance of @{link MailTemplateEngine}
     */
    @Autowired
    private MailTemplateEngine templateEngine;

    @Override
    public void send(Message message) {
        if (!this.isMailSystemEnabled) {
            logger.warn(String.format("Send mail request to recipient [%s] has failed. Mail system is disabled.",
                            StringUtils.join(message.getRecipients(), ',')));

            return;
        }

        try {
            // Set default sender
            if (message.getSender() == null) {
                message.setSender(this.mailSenderDefaultAddress, this.mailSenderDefaultName);
            }

            final MimeMessage mimeMessage = this.createMimeMessage(message);

            this.logMessage(mimeMessage);
            this.mailSender.send(mimeMessage);
        } catch (final ApplicationException appEx) {
            throw appEx;
        } catch (final Exception ex) {
            MailErrorCode code = MailErrorCode.SENT_FAILED;

            String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

            throw ApplicationException.wrap(ex, code, pattern);
        }
    }
    
    @Override
    public String render(EnumOutputFormat format, Message message) throws IllegalArgumentException {
        return this.templateEngine.render(format, message);
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
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();

        final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        mimeMessageHelper.setSubject(message.getSubject());

        if (StringUtils.isBlank(message.getSender().getName())) {
            mimeMessageHelper.setFrom(message.getSender().getAddress());
        } else {
            mimeMessageHelper.setFrom(message.getSender().getAddress(), message.getSender().getName());
        }

        for (final EmailAddress recipient : message.getRecipients()) {
            if (StringUtils.isBlank(recipient.getName())) {
                mimeMessageHelper.addTo(recipient.getAddress());
            } else {
                mimeMessageHelper.addTo(recipient.getAddress(), recipient.getName());
            }
        }

        // TODO: Implement both TEXTand HTML template rendering
        final String htmlContent = this.render(EnumOutputFormat.HTML, message);

        mimeMessageHelper.setText(htmlContent, true);

        return mimeMessage;
    }

    /**
     * Logs an email message.
     *
     * @param message the message to log.
     */
    private void logMessage(MimeMessage message) {
        try {
            final List<String> recipients = new ArrayList<String>();
            for (final Address address : message.getAllRecipients()) {
                recipients.add(address.toString());
            }

            final String text = String.format("Mail sent to [%s] with subject [%s].",StringUtils.join(recipients,  ","), message.getSubject());

            sendMailLogger.info(text);
        } catch(final MessagingException ex) {
        	final MailErrorCode code = MailErrorCode.LOG_FAILED;

        	String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

            throw ApplicationException.wrap(ex, code, pattern);
        }
    }

}
