-- Execute as superuser
-------------------------------------------------
-- CREATE USER admindb PASSWORD 'dbaccess';
-- CREATE USER webserver PASSWORD 'crudaccess';
-- CREATE DATABASE mtcgdb WITH OWNER admindb;
-- CREATE SCHEMA mtcg;

-- ALTER SCHEMA mtcg OWNER TO admindb;
-- GRANT CONNECT ON DATABASE mtcg TO webserver;

-- Execute with admin account
------------------------------------------------
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
    special VARCHAR(50) NOT NULL
);

GRANT SELECT ON specialtype TO webserver;
GRANT ALL ON specialtype TO admindb;

INSERT INTO specialtype VALUES(DEFAULT, 'Goblin');
INSERT INTO specialtype VALUES(DEFAULT, 'Dragon');
INSERT INTO specialtype VALUES(DEFAULT, 'Wizard');
INSERT INTO specialtype VALUES(DEFAULT, 'Ork');
INSERT INTO specialtype VALUES(DEFAULT, 'Knight');
INSERT INTO specialtype VALUES(DEFAULT, 'Kraken');
INSERT INTO specialtype VALUES(DEFAULT, 'Fire elf');


CREATE TABLE card
(
    id serial PRIMARY KEY,
    type int NOT NULL,
    name VARCHAR(200) UNIQUE NOT NULL,
    damage int NOT NULL,
    element int NOT NULL,
    special int,

    CONSTRAINT fk_special FOREIGN KEY (special) REFERENCES specialtype(id),
    CONSTRAINT fk_type FOREIGN KEY (type) REFERENCES cardtype (id)
);

GRANT SELECT ON card TO webserver;
GRANT ALL ON card TO admindb;

-- SELECT * FROM profile;

-- DELETE FROM profile WHERE id = 8;

CREATE TABLE profile
(
    id serial PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(1000) NOT NULL,
    salt VARCHAR(300) NOT NULL,
    coins int DEFAULT 20 NOT NULL,
    playcount int DEFAULT 0 NOT NULL,
    elo int DEFAULT 100 NOT NULL
    -- Implementation WIP
    -- stack JSONB DEFAULT NULL,
    -- deck JSONB DEFAULT NULL
);


ALTER TABLE profile DROP COLUMN deck;

GRANT SELECT, INSERT, UPDATE, DELETE ON profile TO webserver;
GRANT ALL ON profile TO admindb;

CREATE TABLE trade
(
    id serial PRIMARY KEY,
    owner int NOT NULL,
    offering int NOT NULL,
    receiving int,
    locked bool DEFAULT false,

    CONSTRAINT fk_owner FOREIGN KEY (owner) REFERENCES profile (id),
    CONSTRAINT fk_cardOffer FOREIGN KEY (offering) REFERENCES card (id),
    CONSTRAINT fk_cardReception FOREIGN KEY (receiving) REFERENCES card (id)

);


GRANT SELECT, INSERT, UPDATE, DELETE ON trade TO webserver;
GRANT ALL ON trade TO admindb;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO webserver;

