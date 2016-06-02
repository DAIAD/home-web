# Database setup

DAIAD web application requires two databases in order to function properly, one database for storing user metadata e.g. user accounts and smart water meter registrations and one for storing server specific data e.g. job registration and scheduling configuration. By convention the databases names are `daiad` and `daiad-manager` respectively.

The [PostgreSQL](https://www.postgresql.org/) database server version 9.3 is used. Once the databases have been setup and appropriate accounts have been created, the application schema will be automatically populated during the application startup.

The database initialization is performed by [Flyway](https://flywaydb.org/). The database migration scripts can be found in folder [src/main/resources/db/migration](https://github.com/DAIAD/home-web/tree/master/src/main/resources/db/migration). For additional configuration options about database migration see [Configuration](config.html).