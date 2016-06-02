package eu.daiad.web.configuration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 
 * Provides configuration for the DAIAD database by declaring a set of beans. Moreover, it
 * configures the database migration process.
 *
 */
@Configuration
@EnableJpaRepositories(basePackages = { "eu.daiad.web.repository.application" }, entityManagerFactoryRef = "applicationEntityManagerFactory", transactionManagerRef = "applicationTransactionManager")
@EnableTransactionManagement
public class ApplicationPersistenceConfig {

	@Value("${daiad.flyway.locations}")
	private String locations;

	@Value("${daiad.flyway.baseline-version}")
	private String baselineVersion;

	@Value("${daiad.flyway.baseline-description}")
	private String baselineDescription;

	@Bean(name = "applicationDataSource")
	@Primary
	@ConfigurationProperties(prefix = "datasource.default")
	public DataSource applicationDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "applicationEntityManagerFactory")
	@DependsOn("flyway")
	@Primary
	public LocalContainerEntityManagerFactoryBean applicationEntityManagerFactory(EntityManagerFactoryBuilder builder) {
		return builder.dataSource(applicationDataSource()).packages("eu.daiad.web.domain.application")
						.persistenceUnit("default").build();
	}

	@Bean(name = "applicationEntityManager")
	@Primary
	public EntityManager applicationEntityManager(
					@Qualifier("applicationEntityManagerFactory") EntityManagerFactory applicationEntityManagerFactory) {
		return applicationEntityManagerFactory.createEntityManager();
	}

	@Bean(name = "applicationTransactionManager")
	@Primary
	public PlatformTransactionManager applicationTransactionManager(
					@Qualifier("applicationEntityManagerFactory") EntityManagerFactory applicationEntityManagerFactory) {
		return new JpaTransactionManager(applicationEntityManagerFactory);
	}

	/**
	 * Configures the data migration process.
	 * 
	 * @return the object that implements the database migration process 
	 */
	@Bean(name = "flyway", initMethod = "migrate")
	Flyway flyway() {
		Flyway flyway = new Flyway();

		flyway.setBaselineOnMigrate(true);
		flyway.setBaselineDescription(this.baselineDescription);
		flyway.setBaselineVersionAsString(this.baselineVersion);

		flyway.setLocations(this.locations);

		flyway.setDataSource(applicationDataSource());

		return flyway;
	}

}
