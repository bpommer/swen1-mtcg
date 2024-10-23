-- Execute as superuser
CREATE USER admindb PASSWORD 'dbaccess';
CREATE USER webserver PASSWORD 'crudaccess';
CREATE DATABASE mtcg WITH OWNER admindb;
CREATE SCHEMA mtcg;

ALTER SCHEMA mtcg OWNER TO admindb;

-- Execute with admin account

CREATE TABLE card
(
    id serial PRIMARY KEY,
    type VARCHAR(50) NOT NULL CHECK (type IN ('Monster', 'Spell')),
    name VARCHAR(200) UNIQUE NOT NULL,
    dmg int NOT NULL,
    element VARCHAR(50) CHECK (element IN ('Water', 'Fire', 'Normal')),
    special VARCHAR(50),
    CONSTRAINT special_types CHECK
        (special IN ('Goblin', 'Dragon', 'Wizard', 'Ork',
                     'Knight', 'Kraken', 'Fire Elf') OR NULL)
);

GRANT SELECT ON card TO webserver;
GRANT ALL ON card TO admindb;

CREATE TABLE profile
(
    id serial PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(1000) NOT NULL,
    salt VARCHAR(300) NOT NULL,
    coins int DEFAULT 20,
    playcount int DEFAULT 0,
    elo int DEFAULT 100,
    stack JSONB,
    deck JSONB
);

GRANT SELECT, INSERT, UPDATE, DELETE ON profile TO webserver;
GRANT ALL ON profile TO admindb;

CREATE TABLE trade
(
    id serial PRIMARY KEY,
    owner int,
    offering int,
    receiving int,
    locked bool DEFAULT false,
    CONSTRAINT fk_owner FOREIGN KEY (owner) REFERENCES profile (id),
    CONSTRAINT fk_card FOREIGN KEY (offering, receiving) REFERENCES card (id)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON trade TO webserver;
GRANT ALL ON trade TO admindb;

