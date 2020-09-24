package eu.daiad.common.service.mail;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

@Service
public class ThymeleafMailTemplateEngine implements MailTemplateEngine {

    private final SpringTemplateEngine templateEngine;

    private final ApplicationContext applicationContext;
    
    private final String templatePrefix;
    
    public ThymeleafMailTemplateEngine(
		ApplicationContext applicationContext,
		@Value("${daiad.mail.template.prefix}") String templatePrefix
	) {
        this.applicationContext = applicationContext;
        this.templatePrefix = templatePrefix;
        		
        this.templateEngine = this.thymeleafTemplateEngine();
    }

    @Override
    public String render(EnumOutputFormat format, Message message) throws IllegalArgumentException {
		// Initialize locale
		if (StringUtils.isBlank(message.getLocale())) {
			message.setLocale(Locale.ENGLISH);
		}
    	
		final Context ctx = new Context(new Locale(message.getLocale()));

		ctx.setVariables(message.getVariables());

        switch (format) {
            case TEXT :
                final String textTemplate = this.getEffectiveTemplateName(message, ".txt");

                return this.templateEngine.process(textTemplate, ctx);

            case HTML :
                final String htmlTemplate = this.getEffectiveTemplateName(message, ".html");

                return this.templateEngine.process(htmlTemplate, ctx);

            default :
                throw new IllegalArgumentException(String.format("Format %s is not supported!", format));
        }
    }

    private String getEffectiveTemplateName(Message message, String suffix) {
    	final String template = message.getTemplate();
    	final String locale = message.getLocale().toLowerCase();
    	
        Assert.isTrue(!StringUtils.isBlank(template), "Template name must not be null or empty");
        
        String name =  template + "-" + locale;
        
        if (name.endsWith(suffix)) {
            return name;
        }
        return name + suffix;
    }

    private ResourceBundleMessageSource emailMessageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("classpath:/messages-mail");
        return messageSource;
    }

    private SpringResourceTemplateResolver thymeleafTemplateResolver() {
        final SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(this.applicationContext);
        templateResolver.setPrefix(this.templatePrefix);
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private SpringTemplateEngine thymeleafTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(this.thymeleafTemplateResolver());
        templateEngine.setTemplateEngineMessageSource(this.emailMessageSource());
        return templateEngine;
    }

}