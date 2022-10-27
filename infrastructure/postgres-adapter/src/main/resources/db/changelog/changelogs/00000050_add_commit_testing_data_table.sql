create table exposition_storage.commit_testing_data
(
    id                          uuid                                      not null  constraint commit_testing_data_id primary key,
    coverage                    real,
    code_line_count             bigint,
    test_line_count             bigint,
    test_count                  bigint,
    test_type                   varchar(100)                              not null,
    repository_name             varchar(300)                              not null,
    branch_name                 varchar(300)                              not null,
    commit_sha                  varchar(300)                              not null
);
