package eu.daiad.web.configuration;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

@Component
public class CustomBatchConfigurer implements BatchConfigurer {

	private static final Log logger = LogFactory.getLog(CustomBatchConfigurer.class);

	@Autowired
	private BatchProperties properties;

	@Autowired
	@Qualifier("managementDataSource")
	private DataSource dataSource;

	@Autowired
	@Qualifier("managementEntityManagerFactory")
	private EntityManagerFactory entityManagerFactory;

	@Autowired
	@Qualifier("managementTransactionManager")
	private PlatformTransactionManager transactionManager;

	private JobRepository jobRepository;

	private JobLauncher jobLauncher;

	private JobExplorer jobExplorer;

	CustomBatchConfigurer() {
	}

	@Override
	public JobRepository getJobRepository() {
		return this.jobRepository;
	}

	@Override
	public PlatformTransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	@Override
	public JobLauncher getJobLauncher() {
		return this.jobLauncher;
	}

	@Override
	public JobExplorer getJobExplorer() throws Exception {
		return this.jobExplorer;
	}

	@PostConstruct
	public void initialize() {
		try {
			this.transactionManager = createTransactionManager();
			this.jobRepository = createJobRepository();
			this.jobLauncher = createJobLauncher();
			this.jobExplorer = createJobExplorer();
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to initialize Spring Batch", ex);
		}
	}

	private JobExplorer createJobExplorer() throws Exception {
		JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
		jobExplorerFactoryBean.setDataSource(this.dataSource);
		String tablePrefix = this.properties.getTablePrefix();
		if (StringUtils.hasText(tablePrefix)) {
			jobExplorerFactoryBean.setTablePrefix(tablePrefix);
		}
		jobExplorerFactoryBean.afterPropertiesSet();
		return jobExplorerFactoryBean.getObject();
	}

	private JobLauncher createJobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(getJobRepository());
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}

	protected JobRepository createJobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(this.dataSource);
		if (this.entityManagerFactory != null) {
			logger.warn("JPA does not support custom isolation levels, so locks may not be taken when launching Jobs");
			factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
		}
		String tablePrefix = this.properties.getTablePrefix();
		if (StringUtils.hasText(tablePrefix)) {
			factory.setTablePrefix(tablePrefix);
		}
		factory.setTransactionManager(getTransactionManager());
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	protected PlatformTransactionManager createTransactionManager() {
		return this.transactionManager;
	}

}
