package eu.daiad.web.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * Provides configuration for the DAIAD manager database by declaring a set of beans. Moreover, it
 * configures the database migration process.
 *
 */
@Configuration
@EnableJpaRepositories(
    basePackages = { "eu.daiad.web.repository.admin" },
    entityManagerFactoryRef = "managementEntityManagerFactory",
    transactionManagerRef = "managementTransactionManager"
)
@EnableTransactionManagement
public class AdminPersistenceConfig {

    @Value("${flyway.enabled:true}")
    private Boolean flywayEnabled;

	@Value("${daiad.manager.flyway.locations}")
	private String flywayLocations;

	@Value("${daiad.manager.flyway.baseline-version}")
	private String baselineVersion;

	@Value("${daiad.manager.flyway.baseline-description}")
	private String baselineDescription;

    private Map<String, Object> jpaProps = new HashMap<>();

    @Autowired
    private void readJpaProperties(Environment env, PropertiesReader propertyReader)
    {
        Properties p = null;

        String profile = env.getActiveProfiles()[0];
        String path1 = String.format("classpath:config/datasource-management-%s.properties", profile);
        p = propertyReader.read(path1);

        if (p == null)
            p = propertyReader.read("classpath:config/datasource-management.properties");

        if (p != null && !p.isEmpty()) {
            for (Object k: p.keySet())
                jpaProps.put(k.toString(), p.get(k));
        }
    }

	@Bean(name = "managementDataSource")
	@ConfigurationProperties(prefix = "datasource.management")
	public DataSource managementDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "managementEntityManagerFactory")
	@DependsOn("managementFlyway")
	public LocalContainerEntityManagerFactoryBean managementEntityManagerFactory(EntityManagerFactoryBuilder builder)
	{
	    return builder
		    .dataSource(managementDataSource())
		    .packages("eu.daiad.web.domain.admin")
			.properties(jpaProps)
		    .persistenceUnit("management")
			.build();
	}

	@Bean(name = "managementEntityManager")
	public EntityManager managementEntityManager(
		@Qualifier("managementEntityManagerFactory") EntityManagerFactory factory)
	{
		return factory.createEntityManager();
	}

	@Bean(name = "managementTransactionManager")
	public PlatformTransactionManager managementTransactionManager(
		@Qualifier("managementEntityManagerFactory") EntityManagerFactory factory)
	{
		return new JpaTransactionManager(factory);
	}

	/**
	 * Configures the data migration process.
	 *
	 * @return the object that implements the database migration process
	 */
	@Bean(name = "managementFlyway")
	Flyway flyway()
	{
		Flyway flyway = new Flyway();

		flyway.setBaselineOnMigrate(true);
		flyway.setBaselineDescription(baselineDescription);
		flyway.setBaselineVersionAsString(baselineVersion);

		flyway.setLocations(flywayLocations);
		flyway.setDataSource(managementDataSource());

        if (flywayEnabled)
            flyway.migrate();

		return flyway;
	}
}
