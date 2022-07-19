create schema if not exists account;

create table exposition_storage.repository
(
    id                          varchar(100)               not null
        constraint repository_id primary key,
    vcs_id                      varchar(100)               not null,
    creation_date               timestamp(6)               not null,
    last_update_date            timestamp(6)               not null,
    organization_id             varchar(40),
    vcs_organization_name       varchar(100),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);