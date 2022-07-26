create schema if not exists account_storage;

create table account_storage.user
(
    id                          uuid                       not null
        constraint user_id primary key,
    email                       varchar(200)               not null
        constraint unique_email unique,
    organization_id             uuid,
    onboarding_id               uuid                       not null,
    status                      varchar(40)                not null,
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);


create table account_storage.organization
(
    id                          uuid                       not null
        constraint organization_id primary key,
    name                        varchar(200)               not null,
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);

create table account_storage.team
(
    id                          uuid                       not null
        constraint team_id primary key,
    name                        varchar(300)               not null,
    organization_id             uuid,
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null,
    constraint name_organization_id_unique unique (name, organization_id)
);

create table account_storage.onboarding
(
    id                          uuid                       not null
        constraint onboarding_id primary key,
    has_connected_to_vcs        boolean                    not null,
    has_configured_team         boolean                    not null,
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);


