create table "user" (
    id bigint generated by default as identity,
    date timestamp(6) not null,
    email varchar(255),
    password varchar(255) not null,
    roles varchar(255) array not null,
    primary key (id)
);
alter table if exists "user"
    add constraint constraint_unique_user_email unique (email);