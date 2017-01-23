
drop database if exists "daiad";
drop database if exists "daiad-manager";

create database "daiad" with owner daiad template "template-postgis";
create database "daiad-manager" with owner daiad template "template-postgis";

\c "daiad" daiad
\i src/main/resources/db/migration/daiad/V1_0_0__Initialize_DAIAD_database.sql

\c "daiad-manager" daiad
\i src/main/resources/db/migration/daiad-manager/V1_0_0__Initialize_DAIAD_manager_database.sql

