create table account.team
(
    id                          varchar(40)                not null
        constraint team_id primary key,
    name                        varchar(300)               not null,
    organization_id             varchar(40),
    vcs_organization_name       varchar(100),
    technical_creation_date     timestamp(6) default now() not null,
    technical_modification_date timestamp(6) default now() not null,
    constraint name_organization_id_unique unique (name, organization_id)
);

create table exposition_storage.team_to_repository
(
    team_id       varchar(40),
    repository_id integer not null,
    constraint fk_team foreign key (team_id) references account.team (id),
    constraint fk_repository foreign key (repository_id) references exposition_storage.repository (id)
);

