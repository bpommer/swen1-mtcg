

-- Test cards
-- {"Id":"27051a20-8580-43ff-a473-e986b52f297a", "Name":"FireElf", "Damage": 28.0}
-- {"Id":"74635fae-8ad3-4295-9139-320ab89c2844", "Name":"FireSpell", "Damage": 55.0}
-- {"Id":"d6e9c720-9b5a-40c7-a6b2-bc34752e3463", "Name":"Knight", "Damage": 20.0}
-- {"Id":"99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Name":"Dragon", "Damage": 50.0}
-- {"Id":"845f0dc7-37d0-426e-994e-43fc3ac83c08", "Name":"WaterGoblin", "Damage": 10.0}

select * from profile;
select * from card;

-- ownership query

SELECT
    p.id,
    cards->>'Id' AS cid,
    cards->>'Damage' AS dmg,
    c.type,
    cards as obj,
    c.template
FROM profile p
JOIN LATERAL jsonb_array_elements(p.stack) AS cards ON true
JOIN card c
ON (cards->>'Id') = c.id::text
WHERE p.id = 1
AND (cards->>'Damage')::float >= 22
AND (cards->>'Id') = '27051a20-8580-43ff-a473-e986b52f297a'
AND c.type = 1
;

-- Test cards
-- {"Id":"27051a20-8580-43ff-a473-e986b52f297a", "Name":"FireElf", "Damage": 28.0}
-- {"Id":"74635fae-8ad3-4295-9139-320ab89c2844", "Name":"FireSpell", "Damage": 55.0}
-- {"Id":"d6e9c720-9b5a-40c7-a6b2-bc34752e3463", "Name":"Knight", "Damage": 20.0}
-- {"Id":"99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Name":"Dragon", "Damage": 50.0}
-- {"Id":"845f0dc7-37d0-426e-994e-43fc3ac83c08", "Name":"WaterGoblin", "Damage": 10.0}

--insert test card

INSERT INTO card VALUES ('845f0dc7-37d0-426e-994e-43fc3ac83c08',
                         (SELECT id
                          FROM cardtype
                          WHERE type = 'monster'),
                         'WaterGoblin',
                         55.0,
                         (SELECT id
                          FROM element
                          WHERE type = 'Water'),
                         null,
                         '{"Id":"845f0dc7-37d0-426e-994e-43fc3ac83c08", "Name":"WaterGoblin", "Damage": 10.0}'::jsonb);

select * from card;

-- Transfer card to othe account



-- Give user card to stack
UPDATE profile
SET stack = stack || '{"Id":"27051a20-8580-43ff-a473-e986b52f297a", "Name":"FireElf", "Damage": 28.0}'::jsonb
WHERE id = 1;

UPDATE profile
SET stack = stack || '{"Id":"d6e9c720-9b5a-40c7-a6b2-bc34752e3463", "Name":"Knight", "Damage": 20.0}'::jsonb
WHERE id = 1;

UPDATE profile
SET stack = stack || '{"Id":"74635fae-8ad3-4295-9139-320ab89c2844", "Name":"FireSpell", "Damage": 55.0}'::jsonb
WHERE id = 1;


SELECT jsonb_path_query(stack,
                        (('$[*] ? (@.Id == "27051a20-8580-43ff-a473-e986b52f297a"' ||
                            ' && @.Damage >= 20)'))::jsonpath)
FROM profile
    WHERE id = 1;

-- insert test trade

INSERT INTO trade VALUES ('test',
                          1,
                          'd6e9c720-9b5a-40c7-a6b2-bc34752e3463',
                          (SELECT id
                           FROM cardtype
                           WHERE type = 'monster'),
                          5.4,
                        '{"Id":"d6e9c720-9b5a-40c7-a6b2-bc34752e3463", "Name":"Knight", "Damage": 20.0}'::jsonb);

select * from trade;
select * from profile;

-- Execute trade in single query

WITH ls AS (
    SELECT
        jsonb_array_elements(stack) AS st,
        generate_series(1, jsonb_array_length(stack)) AS in
    FROM profile
    WHERE profile.id = 2
    ORDER BY (stack ->>'Id')::UUID
), fl AS (
    SELECT jsonb_agg(ls.st) AS newstack
    FROM ls
    WHERE ls.in != (
        SELECT ls.in
        FROM ls
        WHERE (st->>'Id') = '27051a20-8580-43ff-a473-e986b52f297a'
        ORDER BY ls.in
        LIMIT 1
    )
), uc AS ( -- filter card from buyer stack
    SELECT ls.st AS newstack
    FROM ls
    WHERE ls.in = (
        SELECT ls.in
        FROM ls
        WHERE (st->>'Id') = '27051a20-8580-43ff-a473-e986b52f297a'
        ORDER BY ls.in
        LIMIT 1
    )
), oq AS (
    UPDATE profile
    SET stack = stack || (SELECT uc.newstack FROM uc)
    WHERE id = 1
), tc AS (
    SELECT cardobject AS obj, owner AS cardowner
    FROM trade
    WHERE id = 'test'
    LIMIT 1
), dt AS (
    DELETE FROM trade
    WHERE id = 'test'
    AND owner = (SELECT tc.cardowner FROM tc)
)
UPDATE profile
SET stack = COALESCE(fl.newstack, '[]'::jsonb)  || (tc.obj::jsonb)
FROM fl, tc
WHERE id = 2;

select * from trade;
select * from profile;


update trade
set cardobject = '{"Id": "845f0dc7-37d0-426e-994e-43fc3ac83c08", "Name": "WaterGoblin", "Damage": 10.0}'::jsonb,
    offer = '845f0dc7-37d0-426e-994e-43fc3ac83c08'
where id = 'test';


-- return card from trade
WITH r AS (
    SELECT cardobject AS c
    FROM trade
    WHERE id = 'userTrade'
    LIMIT 1
), dt AS (
    DELETE FROM trade
        WHERE id = 'userTrade'
            AND owner = 2
)
UPDATE profile
SET stack = stack || r.c::jsonb
FROM r
WHERE id = 2;

-- Test cards
-- {"Id":"27051a20-8580-43ff-a473-e986b52f297a", "Name":"FireElf", "Damage": 28.0}
-- {"Id":"74635fae-8ad3-4295-9139-320ab89c2844", "Name":"FireSpell", "Damage": 55.0}
-- {"Id":"d6e9c720-9b5a-40c7-a6b2-bc34752e3463", "Name":"Knight", "Damage": 20.0}
-- {"Id":"99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Name":"Dragon", "Damage": 50.0}
-- {"Id":"845f0dc7-37d0-426e-994e-43fc3ac83c08", "Name":"WaterGoblin", "Damage": 10.0}


update trade
set offer = '845f0dc7-37d0-426e-994e-43fc3ac83c08',
    cardobject = '{"Id":"845f0dc7-37d0-426e-994e-43fc3ac83c08", "Name":"WaterGoblin", "Damage": 10.0}'::jsonb
where id = 'test';

-- user card ownership
SELECT jsonb_path_query(stack,
                        '$[*] ? (@.Id == "27051a20-8580-43ff-a473-e986b52f297a")'::jsonpath)
FROM profile
WHERE id = 1
LIMIT 1;



-- insert card object from card table into user stack
select * from profile;
WITH cd AS (
    SELECT template AS obj
    FROM card
    WHERE id = '845f0dc7-37d0-426e-994e-43fc3ac83c08'
    LIMIT 1
)
UPDATE profile
SET stack = stack || cd.obj
FROM cd
WHERE id = 1;



-- remove single instance of card by ID
-- and insert card from trade
select * from trade;
select * from profile;
select * from card;
WITH ls AS (
    SELECT
        jsonb_array_elements(stack) AS st,
        generate_series(1, jsonb_array_length(stack)) AS in
    FROM profile
    WHERE profile.id = 1
    ORDER BY (stack ->>'Id')::UUID
), fl AS (
    SELECT jsonb_agg(ls.st) AS newstack
    FROM ls
    WHERE ls.in != (
        SELECT ls.in
        FROM ls
        WHERE (st->>'Id') = '27051a20-8580-43ff-a473-e986b52f297a'
        ORDER BY ls.in
        LIMIT 1
    )
), c AS (
    SELECT template AS obj
        FROM card
        WHERE id = '845f0dc7-37d0-426e-994e-43fc3ac83c08'
        LIMIT 1
) UPDATE profile
SET stack = COALESCE(fl.newstack || c.obj, '[]'::jsonb)
FROM fl, c
WHERE id = 1;



SELECT template
FROM card
WHERE id = '845f0dc7-37d0-426e-994e-43fc3ac83c08'
LIMIT 1;



-- remove single instance of card by ID

WITH ls AS (
    SELECT
        jsonb_array_elements(stack) AS st,
        generate_series(1, jsonb_array_length(stack)) AS in
    FROM profile
    WHERE profile.id = 1
    ORDER BY (stack ->>'Id')::UUID
), fl AS (
    SELECT jsonb_agg(ls.st) AS newstack
    FROM ls
    WHERE ls.in != (
        SELECT ls.in
        FROM ls
        WHERE (st->>'Id') = '845f0dc7-37d0-426e-994e-43fc3ac83c08'
        ORDER BY ls.in
        LIMIT 1
    )
) UPDATE profile
SET stack = COALESCE(fl.newstack, '[]'::jsonb)
FROM fl
WHERE id = 1;

-- Remove card from stack and insert new trade
select * from trade;
select * from profile;


WITH ls AS (
    SELECT
        jsonb_array_elements(stack) AS st,
        generate_series(1, jsonb_array_length(stack)) AS in
    FROM profile
    WHERE profile.id = 2
    ORDER BY (stack ->>'Id')::UUID
), fl AS (
    SELECT jsonb_agg(ls.st) AS newstack
    FROM ls
    WHERE ls.in != (
        SELECT ls.in
        FROM ls
        WHERE (st->>'Id') = 'd6e9c720-9b5a-40c7-a6b2-bc34752e3463'
        ORDER BY ls.in
        LIMIT 1
    )
), uc AS (
    SELECT ls.st AS usercard
    FROM ls
    WHERE ls.in = (
        SELECT ls.in
        FROM ls
        WHERE (st->>'Id') = 'd6e9c720-9b5a-40c7-a6b2-bc34752e3463'
        ORDER BY ls.in
        LIMIT 1
    )
    LIMIT 1
), t AS (
    INSERT INTO trade VALUES (
        'userTrade', 2, 'd6e9c720-9b5a-40c7-a6b2-bc34752e3463',
            (SELECT id FROM cardtype WHERE type = 'monster'),
            99.111, (SELECT uc.usercard::jsonb FROM uc)
        )
)
UPDATE profile
SET stack = COALESCE(fl.newstack, '[]'::jsonb)
FROM fl
WHERE id = 2;



-- insert test profile
insert into profile values (
                               default,
                               'user',
                               'test',
                               'salt',
                               default,
                               default,
                               default,
                               'user-mtcgToken',
                               default,
                               default,
                               default,
                               default,
                               default,
                               default
                           );


