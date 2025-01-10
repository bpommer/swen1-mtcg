-- Execute as superuser
/*
 CREATE USER dbadmin WITH PASSWORD 'dbaccess';
 CREATE USER webserver WITH PASSWORD 'crudaccess';

 CREATE DATABASE mtcgdb WITH OWNER dbadmin;
 GRANT CONNECT TO mtcgdb TO webserver;



 */

GRANT ALL ON ALL SEQUENCES IN SCHEMA public to webserver;

CREATE TABLE cardtype (
    id serial PRIMARY KEY,
    type VARCHAR(50) NOT NULL
);

GRANT SELECT ON cardtype TO webserver;
GRANT ALL ON cardtype TO admindb;

INSERT INTO cardtype VALUES(DEFAULT, 'Monster');
INSERT INTO cardtype VALUES(DEFAULT, 'Spell');

CREATE TABLE specialtype (
    id serial PRIMARY KEY,
    type VARCHAR(50) NOT NULL
);

GRANT SELECT ON specialtype TO webserver;
GRANT ALL ON specialtype TO admindb;

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
    type VARCHAR(200) UNIQUE NOT NULL
);

GRANT ALL ON element TO admindb;
GRANT SELECT ON element to webserver;

-- SELECT * FROM element;

INSERT INTO element VALUES (default, 'Normal');
INSERT INTO element VALUES (default, 'Water');
INSERT INTO element VALUES (default, 'Fire');

-- List of all known cards
CREATE TABLE card
(
    id VARCHAR(200) PRIMARY KEY,
    type int NOT NULL,
    name VARCHAR(200) NOT NULL,
    damage float NOT NULL,
    element int NOT NULL,
    special int,
    hash VARCHAR(500) NOT NULL,


    CONSTRAINT fk_special FOREIGN KEY (special) REFERENCES specialtype(id),
    CONSTRAINT fk_type FOREIGN KEY (type) REFERENCES cardtype (id),
    CONSTRAINT fk_element FOREIGN KEY (element) REFERENCES element(id)
);

SELECT * FROM card;

GRANT SELECT, INSERT ON card TO webserver;
GRANT ALL ON card TO admindb;

CREATE TABLE pack
(
    id serial PRIMARY KEY,
    content JSONB NOT NULL
);

GRANT SELECT, INSERT ON pack TO webserver;
GRANT ALL ON pack TO admindb;

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
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(1000) NOT NULL,
    salt VARCHAR(300) NOT NULL,
    coins int DEFAULT 20 NOT NULL,
    playcount int DEFAULT 0 NOT NULL,
    elo int DEFAULT 100 NOT NULL,
    token VARCHAR(200) NOT NULL,
    -- Implementation WIP
    stack JSONB DEFAULT NULL,
    deck JSONB DEFAULT NULL
);

GRANT SELECT, INSERT, UPDATE, DELETE ON profile TO webserver;
GRANT ALL ON profile TO admindb;

CREATE TABLE trade
(
    id VARCHAR(200) PRIMARY KEY,
    owner int NOT NULL,
    offer VARCHAR(200) NOT NULL,
    type int NOT NULL,
    mindamage float NOT NULL,

    CONSTRAINT fk_card FOREIGN KEY (offer) REFERENCES card,
    CONSTRAINT fk_type FOREIGN KEY(type) REFERENCES cardtype

);


GRANT SELECT, INSERT, UPDATE, DELETE ON trade TO webserver;
GRANT ALL ON trade TO admindb;


