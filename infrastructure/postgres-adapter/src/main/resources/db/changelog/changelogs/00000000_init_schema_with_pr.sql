create schema if not exists exposition_storage;

create sequence if not exists exposition_storage.pull_request_sequence;

-- auto-generated definition
create table exposition_storage.pull_request
(
    pk                          bigint                     not null
        constraint pull_request_pk
            primary key,
    vcs_id                      bigint                     not null,
    commit_number               bigint,
    deleted_line_number         bigint,
    added_line_number           bigint,
    creation_date               timestamp(6)               not null,
    last_update_date            timestamp(6)               not null,
    merge_date                  timestamp(6),
    is_merged                   boolean      default false,
    is_draft                    boolean      default false,
    state                       varchar(200),
    vcs_url                     varchar(200),
    title                       varchar(300),
    author_login                varchar(200),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null
);