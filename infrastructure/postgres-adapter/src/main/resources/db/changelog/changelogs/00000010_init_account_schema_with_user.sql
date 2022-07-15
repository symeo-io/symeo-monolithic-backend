create schema if not exists account;

create sequence if not exists account.user;

create table account.user
(
    id                          varchar(40)               not null
        constraint user_id primary key,
    mail varchar(200)                     not null,
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);
