create table account.team
(
    id                          integer                    not null
        constraint team_id primary key,
    name                        varchar(300)               not null,
    organization_id             varchar(40),
    vcs_organization_name       varchar(100),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null,
    constraint name_organization_id_unique unique (name, organization_id)
);

create table account.team_to_repository
(
    team_id       integer not null,
    repository_id integer not null
);

