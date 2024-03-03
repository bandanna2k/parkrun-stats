CREATE DATABASE
IF NOT EXISTS
parkrun_stats;


-- Course
CREATE TABLE
IF NOT EXISTS
parkrun_stats.course (
    course_name             VARCHAR(255)        NOT NULL,
    course_long_name        VARCHAR(255)        NOT NULL,
    country_code            INT                 NOT NULL,
    country                 VARCHAR(2)          NOT NULL,
    status                  CHAR(1)             NOT NULL,

    PRIMARY KEY (course_name)

) DEFAULT CHARSET=utf8mb4;


-- Athlete
CREATE TABLE
IF NOT EXISTS
parkrun_stats.athlete (
    athlete_id      BIGINT              NOT NULL,
    name            VARCHAR(255)            NULL,

    PRIMARY KEY (athlete_id),

    INDEX athlete_index (athlete_id),
    INDEX name_index    (name)

) DEFAULT CHARSET=utf8mb4;


-- Result
CREATE TABLE
IF NOT EXISTS
parkrun_stats.result (
    course_name     VARCHAR(255)        NOT NULL,
    event_number    INT                 NOT NULL,
    position        INT                 NOT NULL,
    athlete_id      BIGINT              NOT NULL,
    time_seconds    INT                 NOT NULL,

    UNIQUE KEY (course_name, event_number, position)

) DEFAULT CHARSET=utf8mb4;


-- Course Event Summary
CREATE TABLE
IF NOT EXISTS
parkrun_stats.course_event_summary (
    course_name             VARCHAR(255)        NOT NULL,
    event_number            INT                 NOT NULL,
    date                    DATE                    NULL,
    finishers               INT                 NOT NULL,
    first_male_athlete_id   BIGINT              NOT NULL,
    first_female_athlete_id BIGINT              NOT NULL,

    UNIQUE KEY (course_name, event_number)

) DEFAULT CHARSET=utf8mb4;


-- ---------------------------------- --
--         NON REGION TABLES
-- ---------------------------------- --

-- Course Event Summary
CREATE TABLE
IF NOT EXISTS
parkrun_stats.course_event_summary_other (
    course_name             VARCHAR(255)        NOT NULL,
    event_number            INT                 NOT NULL,
    date                    DATE                NOT NULL,
    finishers               INT                 NOT NULL,
    first_male_athlete_id   BIGINT              NOT NULL,
    first_female_athlete_id BIGINT              NOT NULL,

    UNIQUE KEY (course_name, event_number)

) DEFAULT CHARSET=utf8mb4;


-- Result
CREATE TABLE
IF NOT EXISTS
parkrun_stats.result_other (
    course_name     VARCHAR(255)        NOT NULL,
    event_number    INT                 NOT NULL,
    position        INT                 NOT NULL,
    athlete_id      BIGINT              NOT NULL,
    time_seconds    INT                 NOT NULL,

    UNIQUE KEY (course_name, event_number, position)

) DEFAULT CHARSET=utf8mb4;
