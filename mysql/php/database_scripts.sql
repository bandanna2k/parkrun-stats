-- Friend session
CREATE TABLE
IF NOT EXISTS
friend_session (
    friend_session_id       INT                 NOT NULL AUTO_INCREMENT,

    athlete_id              INT                 NOT NULL,
    question                INT                 NOT NULL,

    given_answer            INT                 NOT NULL,
    actual_answer           INT                 NOT NULL,

    athlete_id1             INT                 NULL,
    athlete_id2             INT                 NULL,
    athlete_id3             INT                 NULL,
    athlete_id4             INT                 NULL,
    athlete_id5             INT                 NULL,

    athlete_id6             INT                 NULL,
    athlete_id7             INT                 NULL,
    athlete_id8             INT                 NULL,
    athlete_id9             INT                 NULL,

    PRIMARY KEY (friend_session_id)

) DEFAULT CHARSET=utf8mb4;

INSERT INTO friend_session
(friend_session_id, athlete_id, question, given_answer, actual_answer)
VALUES
(1, 414811, 0, 109, 109);

-- Friend counts
CREATE TABLE
IF NOT EXISTS
friend_counts (
    friend_session_id       INT                 NOT NULL,
    athlete_id1             INT                 NOT NULL,
    athlete_id2             INT                 NOT NULL,
    count                   INT                 NOT NULL,

    PRIMARY KEY (friend_session_id, athlete_id1, athlete_id2)

) DEFAULT CHARSET=utf8mb4;

INSERT INTO friend_counts
(athlete_id, friend_athlete_id, count)
VALUES
(1, 322032, 414811, 11),
(1, 322032, 2226179, 77),
(1, 414811, 2226179, 99);


SELECT fc.*
FROM friend_session fs
LEFT JOIN friend_counts fc USING (friend_session_id)
WHERE fs.athlete_id = 414811;