-- Execute as superuser
/*
 CREATE USER dbadmin WITH PASSWORD 'dbaccess';
 CREATE USER webserver WITH PASSWORD 'crudaccess';

 CREATE DATABASE mtcgdb WITH OWNER dbadmin;
 GRANT CONNECT ON DATABASE mtcgdb TO webserver;
 GRANT ALL ON SCHEMA public TO dbadmin;




 */

CREATE EXTENSION IF NOT EXISTS CITEXT;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO webserver;
GRANT ALL ON SCHEMA public TO dbadmin;

CREATE TABLE cardtype (
    id serial PRIMARY KEY,
    type CITEXT UNIQUE NOT NULL
);

GRANT SELECT ON cardtype TO webserver;
GRANT ALL ON cardtype TO dbadmin;

INSERT INTO cardtype VALUES(DEFAULT, 'Monster');
INSERT INTO cardtype VALUES(DEFAULT, 'Spell');


CREATE TABLE specialtype (
    id serial PRIMARY KEY,
    type TEXT UNIQUE NOT NULL
);


GRANT SELECT ON specialtype TO webserver;
GRANT ALL ON specialtype TO dbadmin;

INSERT INTO specialtype VALUES(DEFAULT, 'Goblin');
INSERT INTO specialtype VALUES(DEFAULT, 'Dragon');
INSERT INTO specialtype VALUES(DEFAULT, 'Wizzard');
INSERT INTO specialtype VALUES(DEFAULT, 'Ork');
INSERT INTO specialtype VALUES(DEFAULT, 'Knight');
INSERT INTO specialtype VALUES(DEFAULT, 'Kraken');
INSERT INTO specialtype VALUES(DEFAULT, 'FireElf');

-- SELECT * FROM specialtype;

CREATE TABLE element
(
    id serial PRIMARY KEY,
    type TEXT UNIQUE NOT NULL
);

GRANT SELECT ON element to webserver;
GRANT ALL ON element TO dbadmin;

-- SELECT * FROM element;

INSERT INTO element VALUES (default, 'Normal');
INSERT INTO element VALUES (default, 'Water');
INSERT INTO element VALUES (default, 'Fire');

-- List of all known cards


CREATE TABLE card
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type int NOT NULL,
    name TEXT NOT NULL,
    damage float NOT NULL,
    element int NOT NULL,
    special int,
    template JSONB NOT NULL,

    CONSTRAINT fk_special FOREIGN KEY (special) REFERENCES specialtype(id),
    CONSTRAINT fk_type FOREIGN KEY (type) REFERENCES cardtype (id),
    CONSTRAINT fk_element FOREIGN KEY (element) REFERENCES element(id)
);


-- SELECT * FROM card;

GRANT SELECT, INSERT ON card TO webserver;
GRANT ALL ON card TO dbadmin;

CREATE TABLE pack
(
    id serial PRIMARY KEY,
    content JSON NOT NULL,
    amount int DEFAULT 1 CHECK (amount >= 0)
);


GRANT SELECT, INSERT, UPDATE ON pack TO webserver;
GRANT ALL ON pack TO dbadmin;

-- Card list

-- Script to show all cards

/*select
    c.id,
    t.type AS type,
    c.name,
    c.damage,
    e.name AS element,
    s.special AS special
FROM card c
JOIN cardtype t ON c.type = t.id
JOIN element e on c.element = e.id
JOIN specialtype s on c.special = s.id;*/






-- SELECT * FROM profile;
-- SELECT * FROM specialtype;
-- DELETE FROM profile WHERE id = 8;



CREATE TABLE profile
(
    id serial PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    salt TEXT NOT NULL,
    coins int DEFAULT 20 NOT NULL CHECK (coins >= 0),
    playcount int DEFAULT 0 NOT NULL CHECK (playcount >= 0),
    elo int DEFAULT 100 NOT NULL CHECK (elo >= 0),
    token TEXT NOT NULL,
    stack JSONB DEFAULT '[]'::jsonb NOT NULL,
    deck JSONB DEFAULT '[]'::jsonb NOT NULL,
    name TEXT default NULL,
    bio TEXT DEFAULT NULL,
    image TEXT DEFAULT NULL,
    wins int DEFAULT 0 NOT NULL,
    lastlogin timestamp DEFAULT NOW() NOT NULL,
);

select * from profile;



GRANT SELECT, INSERT, UPDATE ON profile TO webserver;
GRANT ALL ON profile TO dbadmin;


CREATE TABLE trade
(
    id TEXT PRIMARY KEY,
    owner int NOT NULL,
    offer UUID NOT NULL,
    type int NOT NULL,
    mindamage float NOT NULL,
    cardobject JSONB NOT NULL,

    CONSTRAINT fk_card FOREIGN KEY (offer) REFERENCES card,
    CONSTRAINT fk_type FOREIGN KEY(type) REFERENCES cardtype

);


GRANT SELECT, INSERT, UPDATE, DELETE ON trade TO webserver;
GRANT ALL ON trade TO dbadmin;


