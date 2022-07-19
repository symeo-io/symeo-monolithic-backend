create schema if not exists account;

create sequence exposition_storage.repository_sequence;

create table exposition_storage.repository
(
    id                          integer               not null
        constraint repository_id primary key,
    vcs_id                      varchar(100)               not null
        constraint vcs_id unique,
    name                        varchar(300)               not null,
    organization_id             varchar(40),
    vcs_organization_name       varchar(100),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);