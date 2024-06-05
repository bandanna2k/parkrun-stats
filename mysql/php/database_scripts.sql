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
