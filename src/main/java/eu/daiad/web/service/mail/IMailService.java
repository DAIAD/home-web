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

    /**
     * Renders an email in the requested format without sending it
     *
     * @param format The output format of the generated output
     * @param message The message to render
     *
     * @return The output of the rendering operation
     *
     * @throws IllegalArgumentException If the output format is not supported
     */
    String render(EnumOutputFormat format, Message message) throws IllegalArgumentException;

}
