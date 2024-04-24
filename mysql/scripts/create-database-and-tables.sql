CREATE DATABASE
IF NOT EXISTS
parkrun_stats;

CREATE DATABASE
IF NOT EXISTS
weekly_stats;


-- Course
CREATE TABLE
IF NOT EXISTS
course (
    course_id               INT                 NOT NULL  PRIMARY KEY  AUTO_INCREMENT,
    course_name             VARCHAR(255)        NOT NULL,
    course_long_name        VARCHAR(255)        NOT NULL,
    country_code            INT                 NOT NULL,
    country                 VARCHAR(2)          NOT NULL,
    status                  CHAR(1)             NOT NULL,

    CONSTRAINT unique_course_1 UNIQUE (course_name)

) DEFAULT CHARSET=utf8mb4;

-- Athlete
CREATE TABLE
IF NOT EXISTS
athlete (
    athlete_id      BIGINT              NOT NULL,
    name            VARCHAR(255)            NULL,

    PRIMARY KEY (athlete_id),

    INDEX athlete_index (athlete_id),
    INDEX name_index    (name)

) DEFAULT CHARSET=utf8mb4;


-- Result
CREATE TABLE
IF NOT EXISTS
result (
    course_id       INT                 NOT NULL,
    date            DATE                NOT NULL,
    position        INT                 NOT NULL,
    athlete_id      BIGINT              NOT NULL,
    age_group       INT                 NOT NULL,
    age_grade       INT                 NOT NULL,
    time_seconds    INT                 NOT NULL,

    PRIMARY KEY (course_id, date, position)

) DEFAULT CHARSET=utf8mb4;


-- Event volunteer
CREATE TABLE
IF NOT EXISTS
event_volunteer (
    course_id       INT                 NOT NULL,
    date            DATE                NOT NULL,
    athlete_id      BIGINT              NOT NULL,

    PRIMARY KEY (course_id, date, athlete_id)

) DEFAULT CHARSET=utf8mb4;


-- Course Event Summary
CREATE TABLE
IF NOT EXISTS
course_event_summary (
    course_id               INT                 NOT NULL,
    date                    DATE                NOT NULL,
    event_number            INT                 NOT NULL,
    finishers               INT                 NOT NULL,
    first_male_athlete_id   BIGINT              NOT NULL,
    first_female_athlete_id BIGINT              NOT NULL,

    PRIMARY KEY (course_id, date)

) DEFAULT CHARSET=utf8mb4;