alter table export add hidden boolean not null default false;

alter table export add pinned boolean not null default false;

alter table export add "type" character varying(40);

update export set "type" = 'DATA_EXPORT';

alter table export alter column "type" set not null;
