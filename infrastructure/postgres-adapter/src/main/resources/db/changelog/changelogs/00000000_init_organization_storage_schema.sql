create schema if not exists organization_storage;

create table organization_storage.onboarding
(
    id                          uuid                                      not null
        constraint onboarding_id primary key,
    has_connected_to_vcs        boolean                                   not null,
    has_configured_team         boolean                                   not null,
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null
);


create table organization_storage.user
(
    id                          uuid                                      not null
        constraint user_id primary key,
    email                       varchar(200)                              not null
        constraint unique_email unique,
    onboarding_id               uuid                                      not null,
    status                      varchar(40)                               not null,
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null,
    constraint fk_user_onboarding_id foreign key (onboarding_id) references organization_storage.onboarding (id)
);

create table organization_storage.organization
(
    id                          uuid                                      not null
        constraint organization_id primary key,
    name                        varchar(200)                              not null,
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null
);

create table organization_storage.team
(
    id                          uuid                                      not null
        constraint team_id primary key,
    name                        varchar(300)                              not null,
    organization_id             uuid,
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null,
    constraint team_name_organization_id_unique unique (name, organization_id),
    constraint fk_team_organization_id foreign key (organization_id) references organization_storage.organization (id)
);


create table organization_storage.user_to_organization
(
    user_id         uuid not null,
    organization_id uuid not null,
    constraint fk_user_to_organization_user_id foreign key (user_id) references organization_storage.user (id),
    constraint fk_user_to_organization_organization_id foreign key (organization_id) references organization_storage.organization (id)
);

create table organization_storage.team_goal
(
    id                          uuid                                      not null
        constraint team_goal_id primary key,
    standard_code               varchar(200)                              not null,
    value                       varchar(100)                              not null,
    team_id                     uuid                                      not null,
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null,
    constraint fk_team_goal_team_id foreign key (team_id) references organization_storage.team (id),
    constraint team_goal_standard_code_team_id_unique unique (standard_code, team_id)
);

create table organization_storage.organization_settings
(
    id                          uuid                                      not null
        constraint organization_settings_id primary key,
    organization_id             uuid                                      not null,
    tag_regex                   varchar(1000),
    pr_merged_on_branch_regex   varchar(1000),
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null,
    constraint fk_organization_settings_organization_id foreign key (organization_id) references organization_storage.organization (id)
);


create table organization_storage.organization_settings_exclude_branch_regex
(
    organization_settings_id uuid         not null,
    exclude_branch_regex           varchar(300) not null
);