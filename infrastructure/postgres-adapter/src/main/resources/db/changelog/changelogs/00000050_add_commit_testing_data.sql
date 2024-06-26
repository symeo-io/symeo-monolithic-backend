create table exposition_storage.commit_testing_data
(
    id                          uuid                                      not null  constraint commit_testing_data_id primary key,
    organization_id             uuid                                      not null,
    total_branch_count          bigint,
    covered_branches            bigint,
    code_line_count             bigint,
    test_line_count             bigint,
    unit_test_count             bigint,
    integration_test_count      bigint,
    repository_name             varchar(300)                              not null,
    branch_name                 varchar(300)                              not null,
    commit_sha                  varchar(300)                              not null,
    date                        timestamp(6) with time zone default now() not null,
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null
);

create table organization_storage.organization_api_key
(
    id                          uuid                                      not null  constraint commit_testing_data_id primary key,
    organization_id             uuid                                      not null,
    name                        varchar(300)                              not null,
    key                         varchar(300)                              not null constraint organization_api_key_unique unique,
    technical_creation_date     timestamp(6) with time zone default now() not null,
    technical_modification_date timestamp(6) with time zone default now() not null
);
