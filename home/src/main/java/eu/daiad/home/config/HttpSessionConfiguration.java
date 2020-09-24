package eu.daiad.home.config;

import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@EnableJdbcHttpSession(tableName = "web.spring_session_home")
public class HttpSessionConfiguration {

}