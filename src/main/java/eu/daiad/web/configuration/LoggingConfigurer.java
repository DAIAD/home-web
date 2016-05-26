package eu.daiad.web.configuration;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.db.jdbc.ColumnConfig;
import org.apache.logging.log4j.core.appender.db.jdbc.ConnectionSource;
import org.apache.logging.log4j.core.appender.db.jdbc.JdbcAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.error.ApplicationException;

@Component
public class LoggingConfigurer implements InitializingBean {

	@Value("${log4j2.appender.jdbc.name:PostgreSQL}")
	private String appenderName;

	@Value("${log4j2.appender.jdbc.table:log4j_message}")
	private String tableName;

	@Autowired
	ApplicationContext applicationContext;

	@Override
	public void afterPropertiesSet() throws Exception {
		ConnectionSource connectionSource = applicationContext.getBean(ConnectionSource.class);

		// Get Log4j2 configuration
		final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
		final Configuration config = loggerContext.getConfiguration();

		// Declare table columns
		ColumnConfig[] columnConfigs = {
						ColumnConfig.createColumnConfig(config, "account", "%X{session.remote-address}", null, null,
										"false", null),
						ColumnConfig.createColumnConfig(config, "remote_address", "%X{session.username}", null, null,
										"false", null),
						ColumnConfig.createColumnConfig(config, "category", "%X{error.category}", null, null, "false",
										null),
						ColumnConfig.createColumnConfig(config, "code", "%X{error.code}", null, null, "false", null),
						ColumnConfig.createColumnConfig(config, "level", "%level", null, null, "false", null),
						ColumnConfig.createColumnConfig(config, "logger", "%logger", null, null, "false", null),
						ColumnConfig.createColumnConfig(config, "message", "%message", null, null, "false", null),
						ColumnConfig.createColumnConfig(config, "exception", "%rEx{full}", null, null, "false", null),
						ColumnConfig.createColumnConfig(config, "timestamp", null, null, "true", null, null) };

		// Create JDBC appender
		Appender appender = JdbcAppender.createAppender(appenderName, null, null, connectionSource, null, tableName,
						columnConfigs);

		appender.start();
		config.addAppender(appender);

		// Add logger
		AppenderRef ref = AppenderRef.createAppenderRef(appenderName, null, null);
		AppenderRef[] refs = new AppenderRef[] { ref };
		LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.ERROR,
						ApplicationException.class.getCanonicalName(), null, refs, null, config, null);
		loggerConfig.addAppender(appender, null, null);

		config.addLogger(ApplicationException.class.getCanonicalName(), loggerConfig);

		// Refresh context
		loggerContext.updateLoggers();
	}
}
