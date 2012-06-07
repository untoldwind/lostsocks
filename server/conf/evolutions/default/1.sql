
# --- !Ups

CREATE SEQUENCE "s_User_id";

CREATE TABLE "User" (
  "id" int8 NOT NULL,
  "email" varchar(255) DEFAULT NULL,
  "firstname" varchar(255) DEFAULT NULL,
  "hashedPassword" varchar(255) DEFAULT NULL,
  "lastname" varchar(255) DEFAULT NULL,
  "username" varchar(255) NOT NULL,
  PRIMARY KEY ("id"),
  UNIQUE ("username")
) WITH (OIDS=FALSE);

INSERT INTO "User" VALUES (nextval('"s_User_id"'),NULL,NULL,'{SSHA}AGTXGOOT8e81kFc566N+ce7rPVi70haptKnZREedK0pQ5GOUYyFRoXG8i/UC5A4xAzvqvuT+',NULL,'admin');

CREATE SEQUENCE "s_Role_id";

CREATE TABLE "Role" (
  "id" int8 NOT NULL,
  "name" varchar(255) NOT NULL,
  PRIMARY KEY ("id"),
  UNIQUE ("name")
) WITH (OIDS=FALSE);

INSERT INTO "Role" VALUES (nextval('"s_Role_id"'),'Administrators');

CREATE TABLE "User_Role" (
  "userId" int8 NOT NULL,
  "roleId" int8 NOT NULL,
  PRIMARY KEY ("userId", "roleId"),
  FOREIGN KEY ("roleId") REFERENCES "Role" ("id"),
  FOREIGN KEY ("userId") REFERENCES "User" ("id")
) WITH (OIDS=FALSE);

INSERT INTO "User_Role" VALUES (1,1);

# --- !Downs

DROP TABLE IF EXISTS "User_Role";
DROP SEQUENCE "s_Role_id";
DROP TABLE IF EXISTS "Role";
DROP SEQUENCE "s_User_id";
DROP TABLE IF EXISTS "User";
