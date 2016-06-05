# Deployment

Running the web application can be accomplished in several ways. For development, using the `spring-boot` plug-in is the preferred way. Still, the application can be executed as a `jar` file too. For production, it is suggested that a `war` file is created and deployed to an application server.

Running the application using the Spring Boot plug-in can be done with the following command
    
    mvn spring-boot:run
 
Running the application as a standard executable `jar` file can be done with the following command

    java -jar target/web-home-0.0.2-SNAPSHOT.jar

# Known Issues

Deployment using a `war` file has been tested on [Apache Tomcat](http://tomcat.apache.org/) web server version 8.0.33.0. During scheduled job launching, it has been noticed in the log files that jobs have been launching twice resulting in excessive resource usage. The reason was that the application was been deployed twice as explained in a similar situation [Quartz job runs twice when deployed on tomcat 6/Ubuntu 10.04LTS](http://stackoverflow.com/questions/5087510/setting-autodeploy-and-deployonstartup-will-cause-app-be-deployed-twice-on-start). To avoid this problem the configuration file `conf/server.xml` is edited as shown below.

```xml
 <Host name="localhost"  appBase="webapps" deployOnStartup="true"
            unpackWARs="true" autoDeploy="false">
    ...
 </Host>
```