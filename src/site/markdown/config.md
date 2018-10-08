# Overview

In order to successfully deploy DAIAD web application, a few configuration properties must be set. The application configuration properties are stored in several files. These files must be located in folder `/src/main/resources` during the project building phase.

As both a convenience and a reference, an example for each required configuration file can be found in folder [configuration](https://github.com/DAIAD/home-web/tree/master/configuration). Before building the project, the contents of this folder should be copied in `/src/main/resources` and updated appropriately.

Next we present every configuration file along with a short description their properties.

# Logging

`log4j2.xml`: Log4j2 logging system configuration properties. An example is shown below.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Properties>
        <Property name="baseDir">logs</Property>
    </Properties>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout
                pattern="%d{yyyy-MM-dd HH:mm:ss} %20X{session.remote-address} %20X{session.username} %-5p %c{1}:%L - %m%n" />
        </Console>

        <RollingFile name="File" filename="${baseDir}/daiad.log"
            filepattern="${baseDir}/daiad-%d{MM-dd-yyyy}-%i.log.gz">
            <PatternLayout
                pattern="%d{yyyy-MM-dd HH:mm:ss} %20X{session.remote-address} %20X{session.username} %-5p %c{1}:%L - %m%n" />
            <Policies>
                <SizeBasedTriggeringPolicy size="10 MB" />
            </Policies>
            <DefaultRolloverStrategy max="10" />
        </RollingFile>
    </Appenders>
    <Loggers>
        <Logger name="org.hibernate.SQL" level="WARN"></Logger>
        <Root level="INFO">
            <AppenderRef ref="Console" />
            <AppenderRef ref="File" />
        </Root>
    </Loggers>
</Configuration>
``` 

In general no changes are required to this file. By default, the configuration will log messages to the system console and to a file. The folder where log files are saved can be set with the `baseDir` attribute.

The current log file name as well as the naming pattern of the older log files can be configured by the `filename` and `filepattern` attributes respectively in `RollingFile` element.

Additional information on how to configuring Log4j2 can be found at [Log4j2] (https://logging.apache.org/log4j/2.x/manual/appenders.html) official documentation site.

# Job Scheduling

`config/scheduler.properties`: Scheduler configuration properties.

|Property|Description|Default|
|---|---|---|
|pool-size|Number of threads for the ThreadPoolTaskScheduler instance|10|
|thread-name-prefix|Prefix for thread names for job launcher custom TaskExecutor|batch-|

# Mail

`config/mail.properties`: Mail properties. All properties prefixed with `spring.mail` are specific to Spring Boot auto-configuration. The properties prefixed with `spring.mail.properties` are specific to JavaMail API. Additional information for configuration properties can be found at [Spring Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html) and [Java Mail Documentation](https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html).

|Property|Description|Default|
|---|---|---|
|spring.mail.default-encoding|Default mail encoding|UTF-8|
|spring.mail.host|The mail server host address||
|spring.mail.port|The mail server port||
|spring.mail.protocol|The protocol used||
|spring.mail.username|User name if authentication is required||
|spring.mail.password|Password if authentication is required||
|spring.mail.properties.mail.smtps.auth|Authentication is required||
|spring.mail.properties.mail.smtp.ssl.enable|Secure connection is required||
|spring.mail.properties.mail.transport.protocol|The transport protocol used||
|daiad.mail.enabled|Enable mail|false|
|daiad.mail.template.prefix|The path where mail templates are stored|classpath:/mail/templates/|
|daiad.mail.sender.address|Mail sender default address||
|daiad.mail.sender.name|Mail sender default name||

# HBASE

`config/hbase.properties`: HBASE configuration properties. At least the `hbase.zookeeper.quorum` property must be set.

|Property|Description|Default|
|---|---|---|
|hbase.zookeeper.quorum|Comma separated list of servers in the ZooKeeper ensemble||
|hbase.rpc.timeout|This is for the RPC layer to define how long HBase client applications take for a remote call to time out. It uses pings to check connections but will eventually throw a TimeoutException.|10000|
|hbase.client.retries.number|Maximum retries. Used as maximum for all retryable operations such as the getting of a cell's value, starting a row update, etc. Retry interval is a rough function based on hbase.client.pause.|2|
|hbase.client.pause|General client pause value. Used mostly as value to wait before running a retry of a failed get, region lookup, etc.|500|
|zookeeper.recovery.retry|Zookeeper retry count|2|
|zookeeper.recovery.retry.intervalmill|Zookeeper retry wait|200|
|zookeeper.session.timeout|ZooKeeper session timeout in milliseconds. It is used in two different ways. First, this value is used in the ZK client that HBase uses to connect to the ensemble. It is also used by HBase when it starts a ZK server and it is passed as the 'maxSessionTimeout'.|60000|
|hbase.data.time.partitions|Number of HBASE region servers that store time series data|5|
|scanner.cache.size|Number of rows for caching that will be passed to scanners|1000|

# Spring Batch

`config/batch.properties`: Spring Batch configuration properties.

|Property|Description|Default|
|---|---|---|
|spring.batch.job.enabled|Disable automatic execution of registered beans|false|
|spring.batch.initializer.enabled|Initialize Spring Batch database schema|false|
|spring.batch.schema|Schema creation script location|classpath:db/migration/daiad-manager/V1_0_1__Initialize_Spring_Batch_database.sql|
|spring.batch.table-prefix|Prefix used by Spring Batch job repository for schema objects|batch.|
|job.parameters.incrementer.name|Sequence name for DataSourceJobParametersIncrementer implementation|incrementer|
|daiad.batch.server-time-zone|Application server time zone. Used as a workaround to Spring Batch saving local time to PostgreSQL field of type `timestamp without time zone`.|Europe/Athens|

# Application General Settings

`config/application.properties`: Common application properties. The folder specified in property `tmp.folder` must be created with read/write permissions. Moreover, the active profile must be set to either `development` or `production`. Depending on the value of the last property, a configuration file `config/application-development.properties` or `config/application-production.properties` must be also provided.

|Property|Description|Default|
|---|---|---|
|daiad.url|The base url of the web application|[https://app.dev.daiad.eu/](https://app.dev.daiad.eu/)|
|spring.profiles.active|Set active profile|development|
|spring.main.show_banner|Hide Spring boot banner|false|
|logging.config|Log4j configuration properties|classpath:log4j2.xml|
|hbase.properties|HBASE configuration properties source|classpath:config/hbase.properties|
|scheduler.properties|Scheduler properties source|classpath:config/scheduler.properties|
|batch.properties|Batch properties source|classpath:config/batch.properties|
|mail.properties|Mail properties source|classpath:config/mail.properties|
|security.basic.enabled|Disable basic authentication|false|
|spring.thymeleaf.cache|Disable thymeleaf view engine cache|false|
|tmp.folder|Temporary folder for storing files|/tmp/|
|spring.jpa.database|Target database to operate on, auto-detected by default. Can be alternatively set using the "databasePlatform" property.|POSTGRESQL|
|spring.jpa.properties.hibernate.show_sql|Show SQL commands|false|
|spring.jpa.properties.hibernate.format_sql|Format SQL commands|false|
|spring.jpa.properties.hibernate.hbm2ddl.auto|DDL mode. This is actually a shortcut for the "hibernate.hbm2ddl.auto" property. Default to "create-drop" when using an embedded database, "none" otherwise.|validate|
|spring.jpa.properties.hibernate.dialect|SQLDialect JOOQ used when communicating with the configured datasource. For instance `POSTGRES`|org.hibernate.spatial.dialect.postgis.PostgisDialect|
|spring.jpa.hibernate.use-new-id-generator-mappings|Hibernate 5 specific configuration property (should not be chanted)|true|
|spring.jpa.hibernate.naming.strategy|Naming strategy fully qualified name|org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl|
|spring.jpa.hibernate.naming.physical-strategy|Physical naming strategy fully qualified name. SpringNamingStrategy is no longer used as Hibernate 5.1 has removed support for the old NamingStrategy interface. A new SpringPhysicalNamingStrategy is now auto-configured which is used in combination with Hibernate’s default ImplicitNamingStrategy.|org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl|
|error-page|Default error page|/error/403|
|spring.http.multipart.max-file-size|Max file size. Values can use the suffixed "MB" or "KB" to indicate a Megabyte or Kilobyte size.|20Mb|
|spring.http.multipart.max-request-size|Max request size. Values can use the suffixed "MB" or "KB" to indicate a Megabyte or Kilobyte size.|20Mb|
|flyway.enabled|Enable flyway.|false|
|daiad.flyway.baseline-description|Baseline description for the `daiad` database|Database initialization|
|daiad.flyway.baseline-version|Version to start migration for the `daiad` database|1_0_0|
|daiad.flyway.locations|Locations of migrations scripts for the `daiad` database|classpath:db/migration/daiad/|
|daiad.manager.flyway.baseline-description|Baseline description for the `daiad` database|Database initialization|
|daiad.manager.flyway.baseline-version|Version to start migration for the `daiad` database|1_0_2|
|daiad.manager.flyway.locations|Baseline description for the `daiad` database|classpath:db/migration/daiad-manager/|
|daiad.amphiro.validation-string|Enforce constraints for Amphiro measurements|true|
|daiad.docs.require-authentication|Require authentication for documentation and project site|true|
|daiad.password.reset.token.duration|Password reset token duration in hours|3|
|daiad.captcha.google.key|The client site key for Google reCAPTCHA API||
|daiad.amphiro.properties.*|Amphiro b1 default properties||
|spring.messages.basename|Comma-separated list of basenames, each following the ResourceBundle convention|messages,mail-messages|

# Application Profile Specific Settings

`config/application-<profile>.properties`: Additional application configuration properties for the active profile.

|Property|Description|Default|
|---|---|---|
|server.port|Server port. For production profile this property must be commented out|8888|
|server.login.force-https|Enforce HTTPS for the login page. For production profile, it must be set to `true`|true|
|datasource.default.driver-class-name|Data source driver for the `daiad` database|org.postgresql.Driver|
|datasource.default.url|Data source url for the `daiad` database|`jdbc:postgresql://localhost:5432/daiad`|
|datasource.default.username|Data source user for the `daiad` database||
|datasource.default.password|Data source password for the `daiad` database||
|datasource.management.driver-class-name|Data source driver for the `daiad-manager` database|org.postgresql.Driver|
|datasource.management.url|Data source url for the `daiad-manager` database|`jdbc:postgresql://localhost:5432/daiad-manager`|
|datasource.management.username|Data source user for the `daiad-manager` database||
|datasource.management.password|Data source password for the `daiad-manager` database||
|security.white-list|Enables the user name white list filtering|true|


