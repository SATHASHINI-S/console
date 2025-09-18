create table tickets
(
    id          serial primary key,
    title       varchar(100),
    description text,
    level       integer,
    published   boolean
);