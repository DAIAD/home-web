# README

All files and folders in `config-example` must be copied to folder `src/main/resources`. Several parameters must be also set before running the application.

* `log4j2.xml`: Log4j configuration properties.

* `log4j2-test.xml`: Log4j configuration properties for test profile.

* `config/application.properties`: Common application properties. The folder specified in property `tmp.folder` must be created with read/write permissions. Moreover, the active profile must be set to either `development` or `production`.

* `config/application-development.properties`: Additional application configuration properties for the `development` profile. The datasource configuration properties must be set.

* `config/application-production.properties`: Additional application configuration properties for the `production` profile. The datasource configuration properties must be set.

* `config/application-testing.properties`: Additional application configuration properties for testing. The datasource configuration properties must be set.

* `config/batch.properties`: Spring Batch configuration properties.

* `config/hbase.properties`: HBASE configuration properties. At least the `hbase.zookeeper.quorum` property must be set.

* `config/mail.properties`: Mail configuration properties.

* `config/scheduler.properties`: Scheduler configuration properties.
