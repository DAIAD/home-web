package eu.daiad.web.service.mail;

/**
 * A simple service interface for sending mails.
 */
public interface IMailService {

    /**
     * Sends a mail. The mail content is generated from a template. Data is
     * provided using an object. The content of the mail is localized.
     * 
     * @param message the message to send.
     */
    void send(Message message);

}
