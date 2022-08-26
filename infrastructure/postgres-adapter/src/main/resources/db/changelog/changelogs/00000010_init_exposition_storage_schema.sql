create schema exposition_storage;

create table exposition_storage.pull_request
(
    id                          varchar(100)                              not null
        constraint pull_request_id primary key,
    code                        varchar(100)                              not null,
    commit_number               bigint,
    deleted_line_number         bigint,
    added_line_number           bigint,
    creation_date               timestamp(6) with time zone               not null,
    last_update_date            timestamp(6) with time zone               not null,
    merge_date                  timestamp(6) with time zone,
    close_date                  timestamp(6) with time zone,
    start_date_range            varchar(50),
    is_merged                   boolean                     default false,
    is_draft                    boolean                     default false,
    state                       varchar(200),
    vcs_url                     varchar(200),
    title                       varchar(300),
    author_login                varchar(100),
    vcs_repository              varchar(100),
    vcs_repository_id           varchar(100),
    vcs_organization_id         varchar(100),
    branch_name                 varchar(300),
    size                        real,
    days_opened                 real,
    organization_id             uuid,
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null
);

create table exposition_storage.repository
(
    id                          varchar(100)                              not null
        constraint repository_id primary key,
    name                        varchar(300)                              not null,
    organization_id             uuid,
    vcs_organization_id         varchar(100),
    vcs_organization_name       varchar(100),
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null
);

create table exposition_storage.team_to_repository
(
    team_id       uuid         not null,
    repository_id varchar(100) not null,
    constraint fk_team foreign key (team_id) references account_storage.team (id),
    constraint fk_repository foreign key (repository_id) references exposition_storage.repository (id)
);

create sequence exposition_storage.vcs_organization_sequence;

create table exposition_storage.vcs_organization
(
    id                          bigint                                    not null
        constraint vcs_organization_id primary key,
    vcs_id                      varchar(100)                              not null
        constraint vcs_id_vcs_organization unique,
    name                        varchar(300)                              not null,
    organization_id             uuid,
    external_id                 varchar(100),
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null
);

create table exposition_storage.commit
(
    sha                         varchar(200)                              not null
        constraint sha_commit_id primary key,
    pull_request_id             varchar(100)                              not null,
    author_login                varchar(200)                              not null,
    message                     varchar(400),
    date                        timestamp(6) with time zone               not null,
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null
);


