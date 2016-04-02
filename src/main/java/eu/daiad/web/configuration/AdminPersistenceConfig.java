package eu.daiad.web.configuration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
@EnableJpaRepositories(basePackages = { "eu.daiad.web.repository.admin" }, entityManagerFactoryRef = "managementEntityManagerFactory", transactionManagerRef = "managementTransactionManager")
public class AdminPersistenceConfig {

	@Bean(name = "managementDataSource")
	@ConfigurationProperties(prefix = "datasource.management")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "managementEntityManagerFactory")
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

}
