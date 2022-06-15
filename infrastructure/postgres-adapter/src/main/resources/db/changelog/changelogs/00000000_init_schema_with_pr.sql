create schema if not exists exposition_storage;

create sequence if not exists exposition_storage.pull_request_sequence;

-- auto-generated definition
create table exposition_storage.pull_request
(
    id                          varchar(100)               not null
        constraint pull_request_id primary key,
    vcs_id                      bigint                     not null,
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
    team                        varchar(100),
    vcs_repository              varchar(100),
    vcs_organization            varchar(100),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);

create table exposition_storage.pull_request_histogram
(
    start_date_range            varchar(50)                not null,
    organisation           varchar(100)               not null,
    team                   varchar(100)               not null,
    histogram_type              varchar(100)               not null,
    data_below_limit            bigint                     not null,
    data_above_limit            bigint                     not null,

    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null,
    primary key (start_date_range, organisation, team, histogram_type)
)