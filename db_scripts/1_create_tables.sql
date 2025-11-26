-- ==========================================
-- PART 1: AUTHENTICATION DATABASE (auth_db)
-- ==========================================
DROP DATABASE IF EXISTS auth_db;
CREATE DATABASE auth_db;
USE auth_db;

CREATE TABLE users_auth (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    role ENUM('STUDENT', 'INSTRUCTOR', 'ADMIN') NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'LOCKED', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
    last_login TIMESTAMP NULL,

    -- NEW SECURITY COLUMNS (Permanently added)
    failed_attempts INT DEFAULT 0,
    lockout_end TIMESTAMP NULL
);

CREATE INDEX idx_username ON users_auth(username);

-- ==========================================
-- PART 2: ERP DATABASE (erp_db)
-- ==========================================
DROP DATABASE IF EXISTS erp_db;
CREATE DATABASE erp_db;
USE erp_db;

CREATE TABLE students (
    user_id INT PRIMARY KEY,
    roll_number VARCHAR(20) NOT NULL UNIQUE,
    program VARCHAR(100) NOT NULL,
    year INT NOT NULL
);

CREATE TABLE instructors (
    user_id INT PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    title VARCHAR(100),
    employee_id VARCHAR(20) DEFAULT 'TBA'
);

CREATE TABLE courses (
    course_code VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    credits INT NOT NULL
);

CREATE TABLE sections (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL,
    instructor_id INT NULL,
    day_time VARCHAR(100),
    room VARCHAR(50),
    capacity INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    FOREIGN KEY (course_code) REFERENCES courses(course_code),
    FOREIGN KEY (instructor_id) REFERENCES instructors(user_id)
);

CREATE TABLE assessment_schema (
    schema_id INT AUTO_INCREMENT PRIMARY KEY,
    section_id INT NOT NULL,
    component_name VARCHAR(100) NOT NULL,
    max_marks DECIMAL(5,2) NOT NULL,
    weightage DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE
);

CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    status ENUM('REGISTERED', 'DROPPED', 'COMPLETED', 'WAITLISTED') NOT NULL DEFAULT 'REGISTERED',
    final_grade VARCHAR(5) NULL,
    FOREIGN KEY (student_id) REFERENCES students(user_id),
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE RESTRICT,
    UNIQUE KEY uk_student_section (student_id, section_id)
);

CREATE TABLE grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    component_name VARCHAR(100) NOT NULL,
    score DECIMAL(5, 2) NOT NULL,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    UNIQUE KEY unique_grade_entry (enrollment_id, component_name)
);

CREATE TABLE settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL
);