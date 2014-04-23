# --- !Ups

CREATE TABLE Account (
  id bigserial primary key,
  userId varchar(1023) not null unique,
  providerId varchar(1023) not null,
  email varchar(255) NOT NULL unique,
  firstName varchar(255) not null,
  lastName varchar(255) not null,
  fullName varchar(255) not null,
  avatarUrl varchar(255),
  authMethod varchar(1023),

  token varchar(1023),
  secret varchar(1023),

  accessToken varchar(1023),
  tokenType varchar(1023),
  expiresIn int,
  refreshToken varchar(1023),

  hasher varchar(1023),
  password varchar(1023),
  salt varchar(1023),

  created timestamp NOT NULL,
  lastLogin timestamp,
  isActive boolean NOT NULL
);

create table Token (
  id serial primary key NOT NULL,
  uuid char(36) not null,
  email varchar(1023),
  creationTime timestamp not null,
  expirationTime timestamp not null,
  isSignUp boolean not null
);

create table EntryState(
  id bigint primary key not null,
  name varchar(1023)
);

create table Entry (
  id serial primary key not null,
  creatorId bigint not null references Account(id),
  stateId bigint not null references EntryState(id),
  created timestamp not null,
  content text not null
);


# --- !Downs

drop table if exists Entry cascade;
drop table if exists EntryState cascade;
drop table if exists Account cascade;
drop table if exists Token cascade;
