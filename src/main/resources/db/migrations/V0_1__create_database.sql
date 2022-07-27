
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


SET default_tablespace = '';

SET default_table_access_method = heap;


CREATE TABLE main.file (
    id bigint NOT NULL,
    description character varying(255),
    file character varying(255),
    share_link character varying(255),
    upload_date timestamp without time zone,
    owner bigint NOT NULL
);


CREATE SEQUENCE main.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE main.role (
    id bigint NOT NULL,
    name character varying(255)
);


CREATE TABLE main."user" (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    first_name character varying(255),
    last_name character varying(255),
    password character varying(255),
    status character varying(255),
    username character varying(255) NOT NULL
);


CREATE TABLE main.user_role (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL
);


ALTER TABLE ONLY main.file
    ADD CONSTRAINT file_pkey PRIMARY KEY (id);


ALTER TABLE ONLY main.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


ALTER TABLE ONLY main.role
    ADD CONSTRAINT uk_8sewwnpamngi6b1dwaa88askk UNIQUE (name);


ALTER TABLE ONLY main.file
    ADD CONSTRAINT uk_je61f2xw5c4u06n9esxkc7l4 UNIQUE (share_link);


ALTER TABLE ONLY main."user"
    ADD CONSTRAINT uk_ob8kqyqqgmefl0aco34akdtpe UNIQUE (email);


ALTER TABLE ONLY main."user"
    ADD CONSTRAINT uk_sb8bbouer5wak8vyiiy4pf2bx UNIQUE (username);


ALTER TABLE ONLY main."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


ALTER TABLE ONLY main.file
    ADD CONSTRAINT fk93ec5q3s7wdinnymfuwe1dseg FOREIGN KEY (owner) REFERENCES main."user"(id);


ALTER TABLE ONLY main.user_role
    ADD CONSTRAINT fka68196081fvovjhkek5m97n3y FOREIGN KEY (role_id) REFERENCES main.role(id);


ALTER TABLE ONLY main.user_role
    ADD CONSTRAINT fkfgsgxvihks805qcq8sq26ab7c FOREIGN KEY (user_id) REFERENCES main."user"(id);

