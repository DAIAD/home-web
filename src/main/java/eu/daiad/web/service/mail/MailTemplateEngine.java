package eu.daiad.web.service.mail;

public interface MailTemplateEngine {

    String render(EnumOutputFormat format, Message message) throws IllegalArgumentException;

}