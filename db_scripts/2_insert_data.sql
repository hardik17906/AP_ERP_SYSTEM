-- ==========================================
-- 1. SEED AUTH USERS
-- ==========================================
USE auth_db;
DELETE FROM users_auth; -- Clear existing

-- Password is 'password123' for everyone
INSERT INTO users_auth (user_id, username, role, password_hash, status) VALUES
(1, 'admin1', 'ADMIN', '$2a$12$B.R3vRfU/poLkODVlBoGo.uiQQMXfKjNSP.ZYg/OHYn95Z/tv83pK', 'ACTIVE'),
(2, 'inst1', 'INSTRUCTOR', '$2a$12$B.R3vRfU/poLkODVlBoGo.uiQQMXfKjNSP.ZYg/OHYn95Z/tv83pK', 'ACTIVE'),
(3, 'stu1', 'STUDENT', '$2a$12$B.R3vRfU/poLkODVlBoGo.uiQQMXfKjNSP.ZYg/OHYn95Z/tv83pK', 'ACTIVE'),
(4, 'stu2', 'STUDENT', '$2a$12$B.R3vRfU/poLkODVlBoGo.uiQQMXfKjNSP.ZYg/OHYn95Z/tv83pK', 'ACTIVE');

-- ==========================================
-- 2. SEED ERP DATA
-- ==========================================
USE erp_db;

-- Clear existing
DELETE FROM settings;
DELETE FROM grades;
DELETE FROM assessment_schema;
DELETE FROM enrollments;
DELETE FROM sections;
DELETE FROM courses;
DELETE FROM instructors;
DELETE FROM students;

-- A. Profiles
INSERT INTO students (user_id, roll_number, program, year) VALUES
(3, 'B231001', 'B.Tech. CSE', 2025),
(4, 'B231002', 'B.Tech. ECE', 2025);

INSERT INTO instructors (user_id, department, title) VALUES
(2, 'Computer Science', 'Professor');

-- B. Courses
INSERT INTO courses (course_code, title, credits) VALUES
('CS101', 'Introduction to Programming', 4),
('CS301', 'Advanced Programming', 4),
('MATH101', 'Calculus I', 4);

-- C. Sections (For FALL 2025)
INSERT INTO sections (section_id, course_code, instructor_id, day_time, room, capacity, semester, year) VALUES
(101, 'CS101', 2, 'MWF 09:00-10:00', 'C-101', 50, 'FALL', 2025),
(102, 'CS301', 2, 'TTh 11:00-12:30',  'C-102', 35, 'FALL', 2025),
(103, 'MATH101', 2, 'MWF 13:00-14:00', 'A-205', 40, 'FALL', 2025);

-- D. Assessment Schema (Pre-configure CS101 so it looks good in demo)
INSERT INTO assessment_schema (section_id, component_name, max_marks, weightage) VALUES
(101, 'Quiz 1', 20.00, 20.00),
(101, 'Midterm', 100.00, 30.00),
(101, 'Final', 100.00, 50.00);

-- E. Enrollments
-- Student 1 in CS101
INSERT INTO enrollments (enrollment_id, student_id, section_id, status) VALUES
(1, 3, 101, 'REGISTERED');

-- Student 2 in CS101
INSERT INTO enrollments (enrollment_id, student_id, section_id, status) VALUES
(2, 4, 101, 'REGISTERED');

-- F. Grades (Sample Data)
-- Student 1 Scores
INSERT INTO grades (enrollment_id, component_name, score) VALUES
(1, 'Quiz 1', 18.00),
(1, 'Midterm', 85.00);
-- (Final is missing, so average will be partial)

-- G. Settings
INSERT INTO settings (setting_key, setting_value) VALUES
('maintenance_mode', 'false'),
('current_semester', 'FALL'),
('current_year', '2025'),
('registration_deadline', '2025-12-31T23:59:00'),
('drop_deadline', '2025-11-20T23:59:00');