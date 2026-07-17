-- =========================================================
-- Employee Location Tracker - MySQL Schema
-- =========================================================
-- OPTIONAL: You do NOT need to run this manually. With
-- spring.jpa.hibernate.ddl-auto=update in application.properties,
-- Hibernate creates/updates these tables automatically on startup.
--
-- This script is provided only as a reference if you prefer to manage
-- the schema yourself. If you use it, run it in MySQL Workbench / mysql
-- CLI, then set spring.jpa.hibernate.ddl-auto=validate.
--
-- NOTE: Sample users (admin / john.smith / sarah.johnson / michael.brown)
-- are automatically seeded by the application on first startup
-- (see com.employeetracker.config.DataSeeder), using BCrypt-hashed
-- passwords so the documented sample credentials always work.
-- You do not need to insert users manually.
-- =========================================================

CREATE DATABASE IF NOT EXISTS EmployeeTrackerDB
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE EmployeeTrackerDB;

-- =========================================================
-- Table: Users
-- =========================================================
DROP TABLE IF EXISTS EmployeeActivity;
DROP TABLE IF EXISTS EmployeeStops;
DROP TABLE IF EXISTS EmployeeLocation;
DROP TABLE IF EXISTS Users;

CREATE TABLE Users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NULL,
    role            VARCHAR(20)  NOT NULL,
    enabled         TINYINT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at   DATETIME NULL,
    CONSTRAINT UQ_Users_Username UNIQUE (username),
    CONSTRAINT CHK_Users_Role CHECK (role IN ('ADMIN', 'EMPLOYEE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- Table: EmployeeLocation
-- =========================================================
CREATE TABLE EmployeeLocation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    latitude        DOUBLE NOT NULL,
    longitude       DOUBLE NOT NULL,
    accuracy        DOUBLE NULL,
    recorded_at     DATETIME NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_EmployeeLocation_User FOREIGN KEY (user_id)
        REFERENCES Users(id) ON DELETE CASCADE,
    INDEX idx_location_user_time (user_id, recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- Table: EmployeeStops
-- =========================================================
CREATE TABLE EmployeeStops (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    latitude            DOUBLE NOT NULL,
    longitude           DOUBLE NOT NULL,
    start_time          DATETIME NOT NULL,
    end_time            DATETIME NULL,
    duration_minutes    BIGINT NULL,
    ongoing             TINYINT(1) NOT NULL DEFAULT 0,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_EmployeeStops_User FOREIGN KEY (user_id)
        REFERENCES Users(id) ON DELETE CASCADE,
    INDEX idx_stop_user_time (user_id, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- Table: EmployeeActivity
-- =========================================================
CREATE TABLE EmployeeActivity (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    activity_type   VARCHAR(30) NOT NULL,
    description     VARCHAR(255) NULL,
    activity_time   DATETIME NOT NULL,
    CONSTRAINT FK_EmployeeActivity_User FOREIGN KEY (user_id)
        REFERENCES Users(id) ON DELETE CASCADE,
    CONSTRAINT CHK_Activity_Type CHECK (activity_type IN ('LOGIN', 'LOGOUT', 'LOCATION_UPDATE', 'STOP_START', 'STOP_END')),
    INDEX idx_activity_user_time (user_id, activity_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SELECT 'EmployeeTrackerDB schema created successfully.' AS message;
SELECT 'Start the Spring Boot application to auto-seed sample users (admin, john.smith, sarah.johnson, michael.brown).' AS message;
