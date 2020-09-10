package eu.daiad.web.logging;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.logging.log4j.core.appender.db.jdbc.ConnectionSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpringJdbcAppenderConnectionSource implements ConnectionSource {

	@Autowired
	DataSource dataSource;

	@Override
	public Connection getConnection() throws SQLException {
		return this.dataSource.getConnection();
	}

}
