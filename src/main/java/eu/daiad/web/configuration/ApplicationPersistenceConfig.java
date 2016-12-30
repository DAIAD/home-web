package eu.daiad.web.configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import eu.daiad.web.Application;

/**
 *
 * Provides configuration for the DAIAD database by declaring a set of beans. Moreover, it
 * configures the database migration process.
 *
 */
@Configuration
@EnableJpaRepositories(
    basePackages = { "eu.daiad.web.repository.application" }, 
    entityManagerFactoryRef = "applicationEntityManagerFactory", 
    transactionManagerRef = "applicationTransactionManager"
)
@EnableTransactionManagement
public class ApplicationPersistenceConfig {

    @Value("${flyway.enabled:true}")
    private Boolean flywayEnabled;
    
    @Value("${daiad.flyway.locations}")
    private String flywayLocations;

    @Value("${daiad.flyway.baseline-version}")
    private String baselineVersion;

    @Value("${daiad.flyway.baseline-description}")
    private String baselineDescription;
    
    private Map<String, Object> jpaProps = new HashMap<>();
    
    @Autowired
    private void readJpaProperties(Environment env, PropertiesReader propertyReader)
    {
        Properties p = null;
        
        String profile = env.getActiveProfiles()[0];      
        String path1 = String.format("classpath:config/datasource-default-%s.properties", profile);
        p = propertyReader.read(path1);
        
        if (p == null)
            p = propertyReader.read("classpath:config/datasource-default.properties");
        
        if (p != null && !p.isEmpty()) {
            for (Object k: p.keySet())
                jpaProps.put(k.toString(), p.get(k));
        }
    }
    
    @Primary
    @Bean(name = "applicationDataSource")
    @ConfigurationProperties(prefix = "datasource.default")
    public DataSource applicationDataSource() {
        return DataSourceBuilder.create().build();
    }
        
    @Primary
    @Bean(name = "applicationEntityManagerFactory")
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean applicationEntityManagerFactory(EntityManagerFactoryBuilder builder) 
    {            
        return builder
            .dataSource(applicationDataSource())
            .packages("eu.daiad.web.domain.application")
            .properties(jpaProps)
            .persistenceUnit("default")
            .build();  
    }

    @Primary
    @Bean(name = "applicationEntityManager")
    public EntityManager applicationEntityManager(
        @Qualifier("applicationEntityManagerFactory") EntityManagerFactory factory) 
    {
        return factory.createEntityManager();
    }

    @Primary
    @Bean(name = "applicationTransactionManager")
    public PlatformTransactionManager applicationTransactionManager(
        @Qualifier("applicationEntityManagerFactory") EntityManagerFactory factory) 
    {
        return new JpaTransactionManager(factory);
    }

    /**
     * Configures the data migration process.
     *
     * @return the object that implements the database migration process
     */
    @Bean(name = "flyway")
    Flyway flyway() 
    {
        Flyway flyway = new Flyway();

        flyway.setBaselineOnMigrate(true);
        flyway.setBaselineDescription(this.baselineDescription);
        flyway.setBaselineVersionAsString(this.baselineVersion);

        flyway.setLocations(this.flywayLocations);
        flyway.setDataSource(applicationDataSource());

        if (flywayEnabled)
            flyway.migrate();
        
        return flyway;
    }

}
