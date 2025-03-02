

-- Test cards
-- {"Id":"27051a20-8580-43ff-a473-e986b52f297a", "Name":"FireElf", "Damage": 28.0}
-- {"Id":"74635fae-8ad3-4295-9139-320ab89c2844", "Name":"FireSpell", "Damage": 55.0}
-- {"Id":"d6e9c720-9b5a-40c7-a6b2-bc34752e3463", "Name":"Knight", "Damage": 20.0}
-- {"Id":"99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Name":"Dragon", "Damage": 50.0}
-- {"Id":"845f0dc7-37d0-426e-994e-43fc3ac83c08", "Name":"WaterGoblin", "Damage": 10.0}

select * from profile;
select * from card;

--

WITH ls AS (
    SELECT
        jsonb_array_elements(stack) AS st,
        generate_series(1, jsonb_array_length(stack)) AS in
    FROM profile
    WHERE profile.id = ?
    ORDER BY (stack ->>'Id')::UUID
), c1 AS (
    SELECT ls.st, ls.in AS in1
    FROM ls
    WHERE ls.st->>'Id' = '99f8f8dc-e25e-4a95-aa2c-782823f36e2a'
    LIMIT 1
), c2 AS (
    SELECT ls.st, ls.in AS in2
    FROM ls, c1
    WHERE ls.st->>'Id' = 'd6e9c720-9b5a-40c7-a6b2-bc34752e3463'
      AND ls.in != c1.in1
    LIMIT 1
), c3 AS (
    SELECT ls.st, ls.in AS in3
    FROM ls, c1, c2
    WHERE ls.st->>'Id' = '74635fae-8ad3-4295-9139-320ab89c2844'
      AND ls.in NOT IN (c1.in1, c2.in2)
    LIMIT 1
), c4 AS (
    SELECT ls.st, ls.in
    FROM ls, c1, c2, c3
    WHERE ls.st->>'Id' = '74635fae-8ad3-4295-9139-320ab89c2844'
      AND ls.in NOT IN (c1.in1, c2.in2, c3.in3)
    LIMIT 1
)
SELECT COUNT(*) FROM (
    SELECT * FROM c1
    UNION ALL
    SELECT * FROM c2
    UNION ALL
    SELECT * FROM c3
    UNION ALL
    SELECT * FROM c4
) AS count;

select * from card;
WITH ls AS (
    SELECT
        jsonb_array_elements(stack) AS st,
        generate_series(1, jsonb_array_length(stack)) AS in
    FROM profile
    WHERE profile.id = 2
    ORDER BY (stack ->>'Id')::UUID
), c1 AS (
    SELECT ls.st AS card1, ls.in AS in1
    FROM ls
    WHERE st->>'Id' = '99f8f8dc-e25e-4a95-aa2c-782823f36e2a'
    LIMIT 1
), c2 AS (
    SELECT ls.st AS card2, ls.in AS in2
    FROM ls, c1
    WHERE st->>'Id' = 'd6e9c720-9b5a-40c7-a6b2-bc34752e3463'
      AND ls.in != c1.in1
    LIMIT 1
), c3 AS (
    SELECT ls.st AS card3, ls.in AS in3
    FROM ls, c1, c2
    WHERE st->>'Id' = '74635fae-8ad3-4295-9139-320ab89c2844'
      AND ls.in NOT IN (c1.in1, c2.in2)
    LIMIT 1
), c4 AS (
    SELECT ls.st AS card4, ls.in AS in4
    FROM ls, c1, c2, c3
    WHERE st->>'Id' = '74635fae-8ad3-4295-9139-320ab89c2844'
      AND ls.in NOT IN (c1.in1, c2.in2, c3.in3)
    LIMIT 1
), fl AS (
    SELECT jsonb_agg(ls.st) AS newstack
    FROM ls, c1, c2, c3, c4
    WHERE ls.in NOT IN (c1.in1, c2.in2, c3.in3, c4.in4)
)
UPDATE profile
SET deck = '[]'::jsonb || c1.card1 || c2.card2 || c3.card3 || c4.card4,
stack = COALESCE(fl.newStack, '[]'::jsonb)
FROM c1, c2, c3, c4, fl
WHERE id = 2;

update profile
set deck = '[]'::jsonb
where id = 2;


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
select * from profile;

UPDATE profile
SET stack = '[]'::jsonb
WHERE id = 2;

-- Give user card to stack
UPDATE profile
SET stack = stack || '{"Id":"99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Name":"Dragon", "Damage": 50.0}'::jsonb
WHERE id = 2;



UPDATE profile
SET stack = stack || '{"Id":"d6e9c720-9b5a-40c7-a6b2-bc34752e3463", "Name":"Knight", "Damage": 20.0}'::jsonb
WHERE id = 2;

UPDATE profile
SET stack = stack || '{"Id":"74635fae-8ad3-4295-9139-320ab89c2844", "Name":"FireSpell", "Damage": 55.0}'::jsonb
WHERE id = 2;


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

select * from pack;
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


SELECT t.id, t.offer, ct.type, t.mindamage, t.owner
FROM trade t
JOIN cardtype ct
ON ct.id = t.type
WHERE t.id = '6cd85277-4590-49d4-b0cf-ba0a921faad0'
LIMIT 1;


WITH ls AS (
    SELECT
        jsonb_array_elements(stack) AS st,
        generate_series(1, jsonb_array_length(stack)) AS in
    FROM profile
    WHERE profile.id = 1
    ORDER BY (stack ->>'Id')::UUID
), c1 AS (
    SELECT ls.st, ls.in AS in1
    FROM ls
    WHERE ls.st->>'Id' = '644808c2-f87a-4600-b313-122b02322fd5'
    LIMIT 1
), c2 AS (
    SELECT ls.st, ls.in AS in2
    FROM ls, c1
    WHERE ls.st->>'Id' = '4ec8b269-0dfa-4f97-809a-2c63fe2a0025'
      AND ls.in != c1.in1
    LIMIT 1
), c3 AS (
    SELECT ls.st, ls.in AS in3
    FROM ls, c1, c2
    WHERE ls.st->>'Id' = '91a6471b-1426-43f6-ad65-6fc473e16f9f'
      AND ls.in NOT IN (c1.in1, c2.in2)
    LIMIT 1
), c4 AS (
    SELECT ls.st, ls.in
    FROM ls, c1, c2, c3
    WHERE ls.st->>'Id' = 'b017ee50-1c14-44e2-bfd6-2c0c5653a37c'
      AND ls.in NOT IN (c1.in1, c2.in2, c3.in3)
    LIMIT 1
)
SELECT COUNT(*) FROM (
                         SELECT * FROM c1
                         UNION ALL
                         SELECT * FROM c2
                         UNION ALL
                         SELECT * FROM c3
                         UNION ALL
                         SELECT * FROM c4
                     ) AS count



