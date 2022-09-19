create schema job_storage;

create sequence job_storage.job_sequence;

create table job_storage.job
(
    id                          bigint                                    not null
        constraint job_id primary key,
    organization_id             uuid                                      not null,
    team_id                     uuid,
    code                        varchar(200)                              not null,
    status                      varchar(50)                               not null,
    tasks                       jsonb                                     not null,
    end_date                    timestamp(6) with time zone,
    error                       varchar(5000),
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null
);
