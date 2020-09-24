drop database if exists "daiad";

create database "daiad" with owner daiad template "template-postgis";

\c "daiad" daiad
\i src/main/resources/db/migration/daiad/V1_0_0__Initialize_DAIAD_database.sql
