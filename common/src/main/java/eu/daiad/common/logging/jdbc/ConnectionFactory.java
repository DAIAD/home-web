package eu.daiad.common.logging.jdbc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * A static factory for a datasource suitable for a log4j JDBC appender.
 */
public class ConnectionFactory
{
	/** 
     * The configuration prefix
     */
    private static final String PREFIX = "daiad.logging.jdbc.";
    
    private static final int MAX_ACTIVE = 10;
    
    private static final int MAX_IDLE = 8;
    
    private static final int MIN_IDLE = 5;
    
    private static final int INITIAL_SIZE = 5;
    
    // Connection URL properties
    
    private static String url;
    
    private static String driverName;
    
    private static String username;
    
    private static String password;
    
    // Connection pool properties
    
    private static int maxActive;
    
    private static int maxIdle;
    
    private static int minIdle;
    
    private static int initialSize;
    
    static {
        try { 
            readConfig();
        } catch (IOException e) {
            System.err.printf("%s: Failed to load application configuration: %s%n",
                ConnectionFactory.class.getName(), e.getMessage());
        }
    }
    
    /**
     * Read application configuration to load our properties.
     * 
     * <p>
     * The procedure happens independently from Spring's similar procedure, and
     * cannot benefit from it (e.g. by using injected values). This is because log4j will
     * independently configure its subsystem (usually before the IoC container is setup).   
     * 
     * @throws IOException
     */
    private static void readConfig() throws IOException
    {
        Properties props = new Properties();
        Class<?> cls = ConnectionFactory.class;
        
        // Load properties from classpath
        
        try (InputStream in = cls.getResourceAsStream("/config/application.properties")) {
            props.load(in);
        }
        
        String profile = props.getProperty("spring.profiles.active", "development");
        try (InputStream in = cls.getResourceAsStream("/config/application-" + profile + ".properties")) {
            props.load(in);
        }
        
        // Load properties from working directory
        
        try (InputStream in = new FileInputStream("config/application-" + profile + ".properties")) {
            props.load(in);
        } catch (FileNotFoundException ex) { /* no-op */ }
        
        // Read properties for connecting to our JDBC backend.
        // Use Spring's datasource properties as defaults for connection-related properties.
        

        url = props.getProperty("spring.datasource.url");
        
        driverName = props.getProperty("spring.datasource.driver-class-name");
        
        username = props.getProperty("spring.datasource.username");
        
        password = props.getProperty("spring.datasource.password");
        
        // Read connection pool properties
        
        try {
            maxActive = Integer.valueOf(props.getProperty(PREFIX + "max-active"));
        } catch (NumberFormatException e) {
            maxActive = MAX_ACTIVE;
        }
        
        try {
            maxIdle = Integer.valueOf(props.getProperty(PREFIX + "max-idle"));
        } catch (NumberFormatException e) {
            maxIdle = MAX_IDLE;
        }
        
        try {
            minIdle = Integer.valueOf(props.getProperty(PREFIX + "min-idle"));
        } catch (NumberFormatException e) {
            minIdle = MIN_IDLE;
        }
        
        try {
            initialSize = Integer.valueOf(props.getProperty(PREFIX + "initial-size"));
        } catch (NumberFormatException e) {
            minIdle = INITIAL_SIZE;
        }
    }
    
    /**
     * Create a new datasource for logging with a JDBC appender. The datasource is
     * backed by an (independent) connection pool. 
     * 
     * @return
     * @throws IOException
     */
    public static javax.sql.DataSource dataSource()
    {   
        Properties p = new Properties();
        
        p.put("driver-class-name", driverName);
        p.put("username", username);
        p.put("password", password);
        p.put("max-active", maxActive);
        p.put("max-idle", maxIdle);
        p.put("initial-size", initialSize);
        p.put("min-idle", minIdle);
        
		DriverManagerConnectionFactory driverConnectionFactory = new DriverManagerConnectionFactory(url, p);
		
		PoolableConnectionFactory poolConnectionFactory = new PoolableConnectionFactory(driverConnectionFactory, null);
		
		GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(
			poolConnectionFactory
		);
		
		poolConnectionFactory.setPool(pool);
		
        return new PoolingDataSource<>(pool);
    }
}

