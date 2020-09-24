package eu.daiad.utility.config;

import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@EnableJdbcHttpSession(tableName = "web.spring_session_utility")
public class HttpSessionConfiguration {

}