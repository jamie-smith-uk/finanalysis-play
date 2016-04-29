# Thing schema

# --- !Ups
create table "thing" (
  "name" varchar not null,
  "colour" varchar not null
);

# --- !Downs
drop table "thing" if exists;
