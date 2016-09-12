package eu.daiad.web.configuration;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.util.MimeType;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.resourceresolver.SpringResourceResourceResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * Provides configuration settings for sending email. A @{link @PropertySource}
 * with mail configuration settings is registered. Also, the view resolver is
 * configured for rendering mail HTML5 content using the Thymeleaf view engine.
 */
@Configuration
@PropertySource("${mail.properties}")
public class MailConfig {

    /**
     * Thymeleaf properties as configured by the Spring settings.
     */
    private ThymeleafProperties properties;

    /**
     * The application context
     */
    private ApplicationContext applicationContext;

    /**
     * Email template folder.
     */
    @Value("${daiad.mail.template.prefix}")
    private String mailTemplatePrefix;

    MailConfig(ThymeleafProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;

    }

    /**
     * Configures a @{link SpringResourceResourceResolver} to be used as the
     * resolver of the application template resolvers.
     * 
     * @return a new instance of @{link SpringResourceResourceResolver}.
     */
    @Bean
    public SpringResourceResourceResolver thymeleafResourceResolver() {
        SpringResourceResourceResolver resolver = new SpringResourceResourceResolver();
        resolver.setApplicationContext(applicationContext);

        return resolver;
    }

    /**
     * Configures a @{link TemplateResolver} for rendering email HTML5 content.
     * 
     * @return a instance of @{link TemplateResolver}
     */
    private TemplateResolver emailTemplateResolver() {
        TemplateResolver resolver = new TemplateResolver();
        resolver.setResourceResolver(thymeleafResourceResolver());

        resolver.setPrefix(mailTemplatePrefix);
        resolver.setSuffix(properties.getSuffix());

        resolver.setTemplateMode(properties.getMode());

        if (properties.getEncoding() != null) {
            resolver.setCharacterEncoding(properties.getEncoding().name());
        }
        resolver.setCacheable(false);
        resolver.setOrder(1);

        return resolver;
    }

    /**
     * Configures a @{link TemplateResolver} for rendering web page content.
     * 
     * @return a instance of @{link TemplateResolver}
     */
    private TemplateResolver webTemplateResolver() {
        TemplateResolver resolver = new TemplateResolver();
        resolver.setResourceResolver(thymeleafResourceResolver());

        resolver.setPrefix(properties.getPrefix());
        resolver.setSuffix(properties.getSuffix());

        resolver.setTemplateMode(properties.getMode());

        if (properties.getEncoding() != null) {
            resolver.setCharacterEncoding(properties.getEncoding().name());
        }

        resolver.setCacheable(properties.isCache());

        resolver.setOrder(2);

        return resolver;
    }

    /**
     * Registers the template engine bean.
     * 
     * @return an instance of @{link SpringTemplateEngine}
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();

        templateEngine.addTemplateResolver(emailTemplateResolver());
        templateEngine.addTemplateResolver(webTemplateResolver());

        return templateEngine;
    }

    /**
     * Registers the view resolver bean.
     * 
     * @return an instance of @{link ThymeleafViewResolver}
     */
    @Bean
    public ThymeleafViewResolver thymeleafViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setApplicationContext(this.applicationContext);

        resolver.setTemplateEngine(templateEngine());

        resolver.setCharacterEncoding(this.properties.getEncoding().name());
        resolver.setContentType(appendCharset(this.properties.getContentType(), resolver.getCharacterEncoding()));
        resolver.setExcludedViewNames(this.properties.getExcludedViewNames());
        resolver.setViewNames(this.properties.getViewNames());

        // This resolver acts as a fall back resolver (e.g. like a
        // InternalResourceViewResolver) so it needs to have low precedence
        resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 5);
        resolver.setCache(this.properties.isCache());

        return resolver;
    }

    private String appendCharset(MimeType type, String charset) {
        if (type.getCharset() != null) {
            return type.toString();
        }

        LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();

        parameters.put("charset", charset);
        parameters.putAll(type.getParameters());

        return new MimeType(type, parameters).toString();
    }

}
