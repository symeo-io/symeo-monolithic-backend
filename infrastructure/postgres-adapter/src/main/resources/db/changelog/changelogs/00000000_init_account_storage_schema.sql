create schema if not exists account_storage;

create table account_storage.user
(
    id                          varchar(40)                not null
        constraint user_id primary key,
    mail                        varchar(200)               not null
        constraint unique_mail unique,
    organization_id             varchar(40),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);


create table account_storage.organization
(
    id                          varchar(40)                not null
        constraint organization_id primary key,
    name                        varchar(200)               not null,
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);

create table account_storage.team
(
    id                          varchar(40)                not null
        constraint team_id primary key,
    name                        varchar(300)               not null,
    organization_id             varchar(40),
    vcs_organization_name       varchar(100),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null,
    constraint name_organization_id_unique unique (name, organization_id)
);

create table account_storage.onboarding
(
    id                          varchar(40)                not null
        constraint onboarding_id primary key,
    has_connected_to_vcs        boolean                    not null,
    has_configured_team         boolean                    not null,
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);

alter table account_storage.user
    add column onboarding_id varchar(40) not null;

