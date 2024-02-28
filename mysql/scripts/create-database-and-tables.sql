CREATE DATABASE
IF NOT EXISTS
parkrun_stats;

CREATE TABLE
IF NOT EXISTS
parkrun_stats.athlete (
    athlete_id      BIGINT              NOT NULL        PRIMARY KEY,
    name            VARCHAR(255)        NOT NULL,

    INDEX athlete_index (athlete_id),
    INDEX name_index    (name)

) DEFAULT CHARSET=utf8mb4;

CREATE TABLE
IF NOT EXISTS
parkrun_stats.result (
    id              BIGINT              NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    athlete_id      BIGINT              NOT NULL,
    course_name     VARCHAR(255)        NOT NULL,
    event_number    INT                 NOT NULL,
    position        INT                 NOT NULL,
    time            VARCHAR(8)          NOT NULL,

    UNIQUE KEY (athlete_id, course_name, event_number)

) DEFAULT CHARSET=utf8mb4;

CREATE TABLE
IF NOT EXISTS
parkrun_stats.course_event_summary (
    course_name             VARCHAR(255)        NOT NULL,
    event_number            INT                 NOT NULL,
    first_male_athlete_id   BIGINT              NOT NULL,
    first_female_athlete_id BIGINT              NOT NULL,

    PRIMARY KEY (course_name, event_number)

) DEFAULT CHARSET=utf8mb4;