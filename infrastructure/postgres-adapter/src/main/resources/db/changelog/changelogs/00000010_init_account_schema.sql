create schema if not exists account;

create table account.user
(
    id                          varchar(40)                not null
        constraint user_id primary key,
    mail                        varchar(200)               not null
        constraint unique_mail unique,
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);


create table account.organization
(
    id                          varchar(40)                not null
        constraint organization_id primary key,
    name                        varchar(200)               not null,
    external_id                 varchar(50)                not null,
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
)