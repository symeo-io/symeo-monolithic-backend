create table exposition_storage.cycle_time
(
    id                             varchar(100)                not null     constraint cycle_time_id primary key,
    value                          bigint,
    coding_time                    bigint,
    review_time                    bigint,
    time_to_deploy                 bigint,
    deploy_date                    timestamp(6) with time zone,
    pull_request_id                varchar(100)                not null,
    pull_request_author_login      varchar(100)                not null,
    pull_request_state             varchar(100)                not null,
    pull_request_vcs_repository_id varchar(100)                not null,
    pull_request_vcs_repository    varchar(100)                not null,
    pull_request_vcs_url           varchar(100)                not null,
    pull_request_title             varchar(100)                not null,
    pull_request_creation_date     timestamp(6) with time zone not null,
    pull_request_merge_date        timestamp(6) with time zone,
    pull_request_head              varchar(100)                not null
);