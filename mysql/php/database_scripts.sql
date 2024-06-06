-- Friend session
CREATE TABLE
IF NOT EXISTS
friends_session (
    athlete_id              INT                 NOT NULL,

    question                INT                 NOT NULL,
    given_answer            INT                 NOT NULL,
    actual_answer           INT                 NOT NULL,

    url                     VARCHAR(64)         NOT NULL,

    athlete_id1             INT                 NULL,
    athlete_id2             INT                 NULL,
    athlete_id3             INT                 NULL,
    athlete_id4             INT                 NULL,
    athlete_id5             INT                 NULL,

    athlete_id6             INT                 NULL,
    athlete_id7             INT                 NULL,
    athlete_id8             INT                 NULL,
    athlete_id9             INT                 NULL,

    PRIMARY KEY (athlete_id, question),
    INDEX index_url (url)

) DEFAULT CHARSET=utf8mb4;

-- Friend counts
CREATE TABLE
IF NOT EXISTS
friend_counts (
    url                     VARCHAR(64)         NOT NULL,

    athlete_id              INT                 NOT NULL,
    friend_athlete_id       INT                 NOT NULL,
    count                   INT                 NOT NULL,

    PRIMARY KEY (url, athlete_id, friend_athlete_id)

) DEFAULT CHARSET=utf8mb4;


INSERT INTO friend_counts
(url, athlete_id, friend_athlete_id, count)
VALUES
('fe057c269c7ca966893344bc6f45d6aff36aca1c7098d8aee93fa4d8994bab2d', 322032, 414811, 11),
('fe057c269c7ca966893344bc6f45d6aff36aca1c7098d8aee93fa4d8994bab2d', 322032, 2226179, 77),
('fe057c269c7ca966893344bc6f45d6aff36aca1c7098d8aee93fa4d8994bab2d', 414811, 2226179, 99),