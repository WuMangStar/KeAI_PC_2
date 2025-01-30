-- auto-generated definition
create table aicontext
(
    GroupId varchar(35)    not null,
    MsgId   varchar(120)   not null,
    AI      varchar(10240) null,
    primary key (GroupId, MsgId)
);

-- auto-generated definition
create table aisetting
(
    GroupId varchar(32)    not null
        primary key,
    Model   varchar(20)    null,
    Tips    varchar(10240) null
);

-- auto-generated definition
create table onlinetime
(
    Player     varchar(20) not null
        primary key,
    OnlineTime bigint      not null,
    EndTime    timestamp   null
);


-- auto-generated definition
create table usercontext
(
    GroupId varchar(35)    not null,
    MsgId   varchar(120)   not null,
    User    varchar(10240) null,
    time    timestamp      null,
    primary key (GroupId, MsgId)
);

