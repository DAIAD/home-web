# README

All files and folders in `/configuration` must copied to folder `/src/main/resources`. Several parameters must be also set before running the application.

* `log4j.properties`: Log4j configuration properties.

* `config/scheduler.properties`: Scheduler configuration properties.

* `config/hbase.properties`: HBASE configuration properties. At least the `hbase.zookeeper.quorum` property must be set.

* `config/batch.properties`: Spring Batch configuration properties.

* `config/application.properties`: Common application properties. The folder specified in property `tmp.folder` must be created with read/write permissions. Moreover, the active profile must be set to either `development` or `production`.

* `config/application-development.properties`: Additional application configuration properties for the `development` profile. The datasource configuration properties must be set.

* `config/application-production.properties`: Additional application configuration properties for the `production` profile. The datasource configuration properties must be set.

* `config/sql/batch/schema-drop-postgresql.sql`: SQL script for dropping Spring Batch job repository schema.

* `config/sql/batch/schema-postgresql.sql`: SQL script for creating Spring Batch job repository schema.
