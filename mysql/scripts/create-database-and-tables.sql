CREATE DATABASE
IF NOT EXISTS
parkrun_stats;

CREATE TABLE
IF NOT EXISTS
parkrun_stats.result (
    atheleteId  BIGINT              NOT NULL,
    courseName  VARCHAR(255)        NOT NULL,
    eventNumber INT                 NOT NULL,
    position    INT                 NOT NULL,

    PRIMARY KEY (atheleteId, courseName, eventNumber)

) DEFAULT CHARSET=utf8mb4;