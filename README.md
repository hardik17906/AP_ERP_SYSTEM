# University ERP System

A desktop university management application built with Java Swing, MySQL, and Maven. The application separates authentication data from academic data and provides distinct workflows for administrators, instructors, and students.

This was a team project by **Hardik** and **Harsh** for CSE201 Advanced Programming. The original assignment instructions are in [ReadMe.txt](ReadMe.txt).

## What it does

| Role | Main workflows |
| --- | --- |
| Administrator | Manage users, courses, sections, registration deadlines, and maintenance mode. |
| Instructor | View assigned sections, define grading weights, enter grades, and review class statistics. |
| Student | Browse the catalog, register or drop sections, inspect grades and timetables, and export a transcript. |

Authentication uses BCrypt password hashes and a temporary lockout after repeated failed logins. The desktop UI uses FlatLaf; HikariCP manages connections to the separate `auth_db` and `erp_db` databases.

## Run locally

You need MySQL 8, Maven, and a JDK. The original project instructions recommend JDK 21; the Maven compiler settings target Java 11.

1. Run [`db_scripts/1_create_tables.sql`](db_scripts/1_create_tables.sql), then [`db_scripts/2_insert_data.sql`](db_scripts/2_insert_data.sql) in MySQL. The seed script creates demonstration accounts.
2. Copy [`config.properties.example`](src/main/resources/config.properties.example) to `src/main/resources/config.properties`. Enter your **local** MySQL credentials in that ignored file.
3. From the repository root, run:

   ```bash
   mvn test
   mvn exec:java -Dexec.mainClass=edu.univ.erp.Main
   ```

The seeded demo account names are `admin1`, `inst1`, and `stu1`; see the assignment instructions for the test password. Do not use these sample credentials outside a local demo.

## Project structure

- `src/main/java/edu/univ/erp/auth`, `data`, and `service` contain authentication, persistence, and business logic.
- `src/main/java/edu/univ/erp/ui` contains the role-specific Swing screens.
- `src/test/java` contains service tests.
- `db_scripts` contains schema and sample data.
- The repository also includes the [project report](Project_report.pdf), [diagrams](Project_Diagrams.pdf), [test summary](Test_Summary.pdf), and a [demo video link](DEMO_VIDEO.md).

The databases must be available for the application to start. Some tests also require the local database setup.
