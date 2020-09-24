package eu.daiad.common.service.mail;

public interface MailTemplateEngine {

    String render(EnumOutputFormat format, Message message) throws IllegalArgumentException;

}