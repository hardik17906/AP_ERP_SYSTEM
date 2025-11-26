================================================================================
                           UNIVERSITY ERP SYSTEM
                       CSE201: Advanced Programming
================================================================================

TEAM MEMBERS
--------------------------------------------------------------------------------
1. Hardik (Roll No: 2024227)
2. Harsh  (Roll No: 2024234)


SYSTEM REQUIREMENTS
--------------------------------------------------------------------------------
1. Java Development Kit (JDK): Version 21 or higher.
2. Database: MySQL Server 8.0.
3. Build Tool: Maven (for managing dependencies like FlatLaf, HikariCP, etc.).


================================================================================
                           SETUP INSTRUCTIONS
================================================================================

STEP 1: DATABASE SETUP
--------------------------------------------------------------------------------
The application requires two separate databases: 'auth_db' and 'erp_db'.
SQL scripts are provided in the 'db_scripts' folder.

1. Open your MySQL Workbench or Command Line.
2. Run the scripts in the following order:

   A. Run 'db_scripts/1_create_tables.sql'
      - This creates both databases (auth_db, erp_db) and all required tables.

   B. Run 'db_scripts/2_insert_data.sql'
      - This seeds the database with the default Admin, Instructor, and Student
        accounts required for testing.

STEP 2: APPLICATION CONFIGURATION
--------------------------------------------------------------------------------
You must create a configuration file to allow the Java application to connect
to your local MySQL database.

1. Navigate to: src/main/resources/
2. Create a file named: config.properties
3. Paste the following content into it:

   # Auth Database Configuration
   auth.jdbcUrl=jdbc:mysql://localhost:3306/auth_db?allowPublicKeyRetrieval=true&useSSL=false
   auth.username=root
   auth.password=YOUR_MYSQL_PASSWORD_HERE

   # ERP Database Configuration
   erp.jdbcUrl=jdbc:mysql://localhost:3306/erp_db?allowPublicKeyRetrieval=true&useSSL=false
   erp.username=root
   erp.password=YOUR_MYSQL_PASSWORD_HERE

   (IMPORTANT: Replace 'YOUR_MYSQL_PASSWORD_HERE' with your actual MySQL root password)


================================================================================
                           HOW TO RUN
================================================================================

OPTION A: USING INTELLIJ IDEA (Recommended)
--------------------------------------------------------------------------------
1. Open the project folder in IntelliJ.
2. Let Maven resolve dependencies (Look for 'pom.xml').
3. Navigate to: src/main/java/edu/univ/erp/Main.java
4. Right-click 'Main.java' and select "Run 'Main.main()'".

OPTION B: USING COMMAND LINE (Maven)
--------------------------------------------------------------------------------
1. Open a terminal in the project root.
2. Run: mvn clean install
3. Run: mvn exec:java -Dexec.mainClass="edu.univ.erp.Main"


================================================================================
                        DEFAULT LOGIN CREDENTIALS
================================================================================
Use these accounts to test the different roles in the application.
All default accounts use the same password.

+------------+----------+----------------+---------------------------+
| Role       | Username | Password       | Notes                     |
+------------+----------+----------------+---------------------------+
| ADMIN      | admin1   | password123    | Full access, Maintenance  |
| INSTRUCTOR | inst1    | password123    | Computer Science Dept     |
| STUDENT    | stu1     | password123    | B.Tech CSE, Year 2025     |
| STUDENT    | stu2     | password123    | B.Tech ECE, Year 2025     |
+------------+----------+----------------+---------------------------+


================================================================================
                           KEY FEATURES
================================================================================
1. Admin Dashboard:
   - Manage Users (Create Student/Instructor/Admin).
   - Manage Courses & Sections (Room/Time conflict detection).
   - System Settings: Toggle "Maintenance Mode" & set Deadlines.

2. Student Dashboard:
   - View Course Catalog.
   - Register/Drop Sections (Validation checks for capacity & deadlines).
   - View Grades & Download Transcript (CSV export).

3. Instructor Dashboard:
   - View assigned sections only.
   - Configure Grading Schema (Weights must sum to 100%).
   - Gradebook: Enter scores and compute final letter grades.
   - View Class Statistics.

4. Security & Architecture:
   - Two Separate Databases (Auth vs ERP Data).
   - Passwords hashed using BCrypt.
   - Account Lockout: Account locks for 2 minutes after 5 failed attempts.
