create schema exposition_storage;

create sequence exposition_storage.pull_request_sequence;

create table exposition_storage.pull_request
(
    id                          varchar(100)               not null
        constraint pull_request_id primary key,
    vcs_id                      varchar(100)                     not null
        constraint vcs_id_pull_request unique,
    commit_number               bigint,
    deleted_line_number         bigint,
    added_line_number           bigint,
    size                        bigint,
    days_opened                 bigint,
    creation_date               timestamp(6)               not null,
    last_update_date            timestamp(6)               not null,
    merge_date                  timestamp(6),
    start_date_range            varchar(50),
    is_merged                   boolean      default false,
    is_draft                    boolean      default false,
    state                       varchar(200),
    vcs_url                     varchar(200),
    title                       varchar(300),
    author_login                varchar(100),
    vcs_repository              varchar(100),
    vcs_organization            varchar(100),
    organization                varchar(100),
    team                        varchar(100),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);

create table exposition_storage.pull_request_histogram
(
    start_date_range            varchar(50)                not null,
    organization                varchar(100)               not null,
    team                        varchar(100)               not null,
    histogram_type              varchar(100)               not null,
    data_below_limit            bigint                     not null,
    data_above_limit            bigint                     not null,

    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null,
    primary key (start_date_range, organization, team, histogram_type)
);

create sequence exposition_storage.repository_sequence;

create table exposition_storage.repository
(
    id                          bigint                     not null
        constraint repository_id primary key,
    vcs_id                      varchar(100)               not null
        constraint vcs_id_repository unique,
    name                        varchar(300)               not null,
    organization_id             varchar(40),
    vcs_organization_name       varchar(100),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);

create table exposition_storage.team_to_repository
(
    team_id       varchar(40),
    repository_id bigint not null,
    constraint fk_team foreign key (team_id) references account.team (id),
    constraint fk_repository foreign key (repository_id) references exposition_storage.repository (id)
);

create sequence exposition_storage.vcs_organization_sequence;

create table exposition_storage.vcs_organization
(
    id                          bigint                     not null
        constraint vcs_organization_id primary key,
    vcs_id                      varchar(100)               not null
        constraint vcs_id_vcs_organization unique,
    name                        varchar(300)               not null,
    organization_id             varchar(40),
    external_id                 varchar(100),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);


