create table users
(
    id            serial            not null
        constraint users_pkey
            primary key,
    username      varchar(20)       not null,
    password      varchar(32)       not null,
    beannum       integer default 0 not null,
    halfcost      integer default 0,
    doubleearning integer default 0
);

alter table users
    owner to postgres;

