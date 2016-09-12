package eu.daiad.web.service.mail;

public class Message {

    private EmailAddress sender;

    private EmailAddress[] recipients;

    private String Subject;

    private String locale;

    private String template;

    private Object model;

    public EmailAddress getSender() {
        return sender;
    }

    public void setSender(EmailAddress sender) {
        this.sender = sender;
    }

    public void setSender(String address, String name) {
        this.sender = new EmailAddress(address, name);
    }

    public EmailAddress[] getRecipients() {
        return recipients;
    }

    public void setRecipients(EmailAddress[] recipients) {
        this.recipients = recipients;
    }

    public void setRecipients(String address, String name) {
        this.recipients = new EmailAddress[] { new EmailAddress(address, name) };
    }

    public void setRecipients(String address) {
        this.recipients = new EmailAddress[] { new EmailAddress(address) };
    }
    
    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Object getModel() {
        return model;
    }

    public void setModel(Object model) {
        this.model = model;
    }

}
