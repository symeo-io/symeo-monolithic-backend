create table account.onboarding
(
    id                          varchar(40)                not null
        constraint onboarding_id primary key,
    has_connected_to_vcs        boolean                    not null,
    has_configured_team         boolean                    not null,
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);

alter table account.user
    add column onboarding_id varchar(40) not null;

