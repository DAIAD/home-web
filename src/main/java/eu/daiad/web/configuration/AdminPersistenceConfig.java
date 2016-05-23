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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = { "eu.daiad.web.repository.admin" }, entityManagerFactoryRef = "managementEntityManagerFactory", transactionManagerRef = "managementTransactionManager")
@EnableTransactionManagement
public class AdminPersistenceConfig {

	@Value("${daiad.manager.flyway.locations}")
	private String locations;

	@Value("${daiad.manager.flyway.baseline-version}")
	private String baselineVersion;

	@Value("${daiad.manager.flyway.baseline-description}")
	private String baselineDescription;

	@Bean(name = "managementDataSource")
	@ConfigurationProperties(prefix = "datasource.management")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "managementEntityManagerFactory")
	@DependsOn("managementFlyway")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
					@Qualifier("managementDataSource") DataSource dataSource) {
		return builder.dataSource(dataSource).packages("eu.daiad.web.domain.admin").persistenceUnit("management")
						.build();
	}

	@Bean(name = "managementEntityManager")
	public EntityManager entityManager(
					@Qualifier("managementEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return entityManagerFactory.createEntityManager();
	}

	@Bean(name = "managementTransactionManager")
	public PlatformTransactionManager managementTransactionManager(
					@Qualifier("managementEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean(name = "managementFlyway", initMethod = "migrate")
	Flyway flyway() {
		Flyway flyway = new Flyway();

		flyway.setBaselineOnMigrate(true);
		flyway.setBaselineDescription(this.baselineDescription);
		flyway.setBaselineVersionAsString(this.baselineVersion);

		flyway.setLocations(this.locations);

		flyway.setDataSource(dataSource());

		return flyway;
	}
}
