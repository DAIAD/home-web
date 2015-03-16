# README

This repository contains the code for the DAIAD@home web client

## Building

In order to build the project maven is required.

`mvn package`

## Configuration

Configure application by updating settings in `application.settings`. At least the following parameter must be set:

* hbase.zookeeper.quorum: The HBASE zookeeper quorum expressed as a comma separated list of IP addresses or host names.

Moreover, the application expects two tables in HBASE, namely `swm` and `meter`, for storing smart water meter and Amphiro device data. Each table has a single column family named `m`. The names of the tables and column family can be changed by the following parameters:

* hbase.data.swm.table: The HBASE table for storing smart water meter data
* hbase.data.amphiro.table: The HBASE table for storing Amphiro device data
* hbase.data.amphiro.column-family: The column family name, common for both tables


## Running

### Using the spring-boot maven plugin ###

    mvn spring-boot:run
 
### Running as a java application ###

    java -jar target/web-home-0.0.1-SNAPSHOT.jar

### Deploy as a WAR file ###

In order to deploy the application as a web application archive file, edit `pom.xml` and change packaging to `war` and  uncomment the declaration for dependency `spring-boot-starter-tomcat`.
