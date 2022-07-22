create schema job_storage;

create sequence job_storage.job_sequence;

create table job_storage.job
(
    id                          bigint                     not null
        constraint job_id primary key,
    organization_id             varchar(40)                not null,
    code                        varchar(200)               not null,
    status                      varchar(50)                not null,
    end_date                    timestamp(6),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);