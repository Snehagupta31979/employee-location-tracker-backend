# Employee Location Tracker

Enterprise-grade Spring Boot application for real-time employee GPS tracking, stop detection, activity timelines, and admin reporting.

## Project Overview

This system enables organizations to track employee locations in real time using browser GPS, visualize movement on OpenStreetMap via Leaflet.js, detect stops, calculate daily travel distance using the Haversine formula, and generate exportable reports.

**Technology Stack**

- Backend: Java 21, Spring Boot 3.3.5, Spring MVC, Spring Data JPA, Spring Security
- Frontend: HTML5, CSS3, Bootstrap 5, JavaScript, Fetch API
- Database: MySQL 8.x
- Maps: Leaflet.js + OpenStreetMap
- Authentication: Session-based (HTTP Session + Cookies)
- Build: Maven
- Excel export: Apache POI

## Features

**Authentication** — Employee/Admin login, session-based auth (no JWT), role-based access control, secure logout with session invalidation.

**Employee Dashboard** — Welcome message, current tracking status (Online/Moving/Stopped/Offline), today's travelled distance, current lat/lng, last-updated timestamp, interactive Leaflet map, today's activity timeline, manual refresh button, auto GPS update every 10 minutes.

**Admin Dashboard** (same page, shown only for `ADMIN` role) — Total/Online/Offline counts, employee list with status/distance/last-updated, "View" button to focus the map on a selected employee, live locations on a shared map.

**GPS Tracking** — Browser geolocation capture (lat/lng/accuracy/timestamp), auto-update every 10 minutes, manual refresh, all updates persisted to MySQL.

**Distance Calculation** — Haversine formula between consecutive points; today's total distance shown in km.

**Stop Detection** — Detects a stop when an employee remains within ~30 meters for 10+ minutes; stores start/end time and duration; shown on the dashboard.

**Activity Timeline** — Login time, location updates, stop start/end, logout time.

**Reports** — Today's locations/distance/stops, per-employee and per-date reports, Excel (.xlsx) export, print support.

## Folder Structure

```
employee-location-tracker/
├── pom.xml
├── README.md
├── sql/
│   └── schema.sql
└── src/
    └── main/
        ├── java/com/employeetracker/
        │   ├── EmployeeLocationTrackerApplication.java
        │   ├── config/
        │   │   ├── CustomUserDetailsService.java
        │   │   ├── DataSeeder.java
        │   │   ├── PasswordConfig.java
        │   │   ├── SecurityConfig.java
        │   │   ├── TrackingProperties.java
        │   │   └── WebConfig.java
        │   ├── controller/
        │   │   ├── AdminController.java
        │   │   ├── AuthController.java
        │   │   └── LocationController.java
        │   ├── dto/
        │   ├── entity/
        │   ├── exception/
        │   ├── repository/
        │   ├── service/
        │   └── util/
        └── resources/
            ├── application.properties
            └── static/
                ├── index.html
                ├── dashboard.html
                ├── css/style.css
                └── js/
                    ├── api.js
                    ├── auth.js
                    ├── dashboard.js
                    └── map.js
```

## Database Setup

### 1. Install MySQL

Install MySQL Community Server 8.x. During setup, note the **root password** you choose (or create a dedicated user) — you'll need it below. MySQL Workbench is optional but useful for browsing the data.

### 2. Create the database (optional)

You do **not** have to create the database or tables by hand — the app is configured with `spring.jpa.hibernate.ddl-auto=update`, so Hibernate creates `EmployeeTrackerDB` and all its tables automatically the first time it starts (the JDBC URL also has `createDatabaseIfNotExist=true`).

If you'd rather create it yourself, open a MySQL client (Workbench or the `mysql` CLI) and run the whole `sql/schema.sql` script. This creates the `EmployeeTrackerDB` database and the `Users`, `EmployeeLocation`, `EmployeeStops`, and `EmployeeActivity` tables with their primary keys, foreign keys, and indexes. If you go this route, set `spring.jpa.hibernate.ddl-auto=validate` in `application.properties` instead of `update`.

> **Note:** Sample users are **not** inserted by the SQL script. On first startup, `com.employeetracker.config.DataSeeder` automatically creates the sample accounts below with BCrypt-hashed passwords, so the documented credentials always work regardless of how the schema was created. Set `tracking.seed.enabled=false` in `application.properties` to disable this behavior (e.g. in production).

## Configure `application.properties`

Edit `src/main/resources/application.properties` and set your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/EmployeeTrackerDB?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Qwerty@123
```

Adjust:
- `localhost:3306` → your MySQL host and port (3306 is the MySQL default)
- `username` / `password` → your actual MySQL credentials

Tracking settings (optional):

```properties
tracking.stop.radius-meters=30
tracking.stop.duration-minutes=10
tracking.online.threshold-minutes=15
tracking.auto-update.interval-minutes=10
```

`spring.jpa.hibernate.ddl-auto` defaults to `update`, so Hibernate creates/updates the schema for you automatically — no manual SQL needed for a first run. Switch it to `validate` only if you've run `sql/schema.sql` yourself and want Hibernate to just check the schema instead of modifying it.

## How to Run in Eclipse

1. **Import the project**: `File → Import → Maven → Existing Maven Projects` → browse to the extracted `employee-location-tracker` folder → `Finish`. Eclipse will read `pom.xml` and download dependencies automatically (needs an internet connection the first time).
2. **Make sure MySQL is running** locally (Windows: check the "MySQL80" service in Services; or start it from MySQL Workbench).
3. **Set your credentials** in `src/main/resources/application.properties` (see above) to match your local MySQL root/user password.
4. **Run the app**: right-click `EmployeeLocationTrackerApplication.java` (in `src/main/java/com/employeetracker`) → `Run As → Java Application`.
5. Watch the Console — you should see Spring Boot's startup banner, Hibernate creating tables, and log lines like `Seeded user 'admin' with role ADMIN`. When you see `Started EmployeeLocationTrackerApplication`, it's up.
6. Open a browser to `http://localhost:8080`.

This project does **not** use Lombok — all getters, setters, constructors, and builders are hand-written, so it compiles and runs in any IDE (Eclipse, IntelliJ, VS Code) with zero extra plugins or annotation-processing setup.

## How to Run from the Command Line

**Prerequisites:** Java 21 JDK, Maven 3.9+, MySQL Community Server 8.x running locally, a modern browser with GPS support (Chrome recommended).

**Steps**

1. Extract the project
2. Make sure MySQL is running and `application.properties` has the right username/password
3. Build and run:

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/employee-location-tracker-1.0.0.jar
```

4. Open your browser: `http://localhost:8080`

## Default Login Credentials

| Role     | Username        | Password    |
|----------|-----------------|-------------|
| Admin    | admin           | password123 |
| Employee | john.smith      | password123 |
| Employee | sarah.johnson   | password123 |
| Employee | michael.brown   | password123 |

## REST API Endpoints

**Authentication**

| Method | Endpoint            | Description       |
|--------|---------------------|--------------------|
| POST   | /api/auth/login     | Login              |
| POST   | /api/auth/logout    | Logout             |
| GET    | /api/auth/me        | Current user info  |

**Employee APIs**

| Method | Endpoint                  | Description                  |
|--------|---------------------------|-------------------------------|
| POST   | /api/location/save        | Save GPS location             |
| GET    | /api/location/current     | Current location              |
| GET    | /api/location/history     | Location history (`?date=`)   |
| GET    | /api/location/distance    | Today's distance              |
| GET    | /api/location/stops       | Today's stops                 |
| GET    | /api/location/activities  | Today's activity timeline     |

**Admin APIs**

| Method | Endpoint                     | Description                          |
|--------|------------------------------|---------------------------------------|
| GET    | /api/admin/employees         | List all employees                     |
| GET    | /api/admin/employee/{id}     | Employee details                       |
| GET    | /api/admin/live-locations    | All live locations                     |
| GET    | /api/admin/summary           | Online/offline summary                 |
| GET    | /api/admin/report            | Generate report (`?employeeId=&date=`) |
| GET    | /api/admin/report/export     | Export report to Excel                 |

## Troubleshooting Guide

**Cannot connect to MySQL**
- Verify MySQL is running: `services.msc` → look for `MySQL80` (Windows), or `sudo systemctl status mysql` (Linux/Mac)
- Confirm port 3306 is open and not used by another instance
- Double-check `spring.datasource.username` / `spring.datasource.password` in `application.properties` match your MySQL login
- If you see `Public Key Retrieval is not allowed`, make sure `allowPublicKeyRetrieval=true` is in the JDBC URL (it already is by default in this project)

**Login fails with correct credentials**
- Confirm the application has started at least once so `DataSeeder` can create the sample users
- Passwords are BCrypt-hashed; the default is `password123`

**GPS not working**
- Use HTTPS or `localhost` (browsers require a secure context for geolocation in production)
- Allow the location permission when prompted
- The `EMPLOYEE` role is required for `/api/location/save`

**Map not loading**
- Check your internet connection (OpenStreetMap tiles require network access)
- Ensure the Leaflet CDN is not blocked by a firewall

**403 Forbidden on API calls**
- The session may have expired; log in again
- The CSRF token is read from the `XSRF-TOKEN` cookie and sent automatically as the `X-XSRF-TOKEN` header on POST requests by `js/api.js`

**Build fails**
- Ensure Java 21 is installed: `java -version`
- Set `JAVA_HOME` to your JDK 21 path
- Run `mvn clean compile` to see detailed errors

**Hibernate validate fails on startup**
- Run `sql/schema.sql` first
- Ensure `spring.jpa.hibernate.ddl-auto=validate` matches the existing schema

## IntelliJ IDEA / STS Setup

1. File → Open → select the project folder
2. Wait for the Maven import to complete
3. Set Project SDK to Java 21
4. Run `EmployeeLocationTrackerApplication`'s `main` method
