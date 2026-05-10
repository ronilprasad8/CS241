# CS241 Assignment Two: School Administration System (SAS)

Contributors:Ranveer Singh (S11230141)
             Ronil Prasad (S11231541)  
             Shivan Prasad (S11231502) 
             Mohammed Suhail (S11230995)
             Mohammed Afeef (S11229568)
             
Course: CS241 - Software Design and Implementation  
University: The University of the South Pacific  
Date: 14 October 2025

![Java](https://img.shields.io/badge/Java-17+-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-orange.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

                           Assignment Overview

A comprehensive and robust Spring Boot web application designed for efficient school administration management. This project provides a secure, user-friendly platform for handling user accounts (students, teachers, and administrators), class assignments, subject enrollments, attendance tracking, grade management, and more. Built with modern Java technologies, it emphasizes security, scalability, and ease of use, making it an ideal solution for educational institutions to streamline their administrative processes.

                           Table of Content
- Features
- Core Concepts & Design
- Project Structure
- Getting Started
  - Dependencies
- Usage
  - User Roles and Dashboards
  - Key Functionalities
  - Database Initialization
- File Formats Supported
- Credits & References

                           System Features

1. Multi-User Role Management

The application supports three primary user roles: Administrators, Teachers, and Students, each with tailored access and functionalities to ensure secure and appropriate data handling.

2. Secure Authentication and Authorization

Integrated Spring Security for robust login, password encoding, and role-based access control. Custom login success handler directs users to role-specific dashboards upon authentication.

3. Comprehensive User Management

Administrators can create, update, and delete user accounts for students and teachers, including detailed profile information, class assignments, and subject enrollments.

4. Class and Subject Administration

Manage school classes by year level and stream, and subjects categorized appropriately. Supports dynamic assignment of subjects to students and teachers.

5. Attendance Tracking

Teachers can mark and view attendance for their assigned classes, while students can check their own attendance records. Administrators have oversight across all attendance data.

6. Grade Management

Teachers can enter and update grades for students in their subjects. Students can view their grades and academic performance.

7. Enrollment Management

Handle student enrollments in subjects, with automatic grade initialization and support for updates.

8. Responsive Web Interface

Built with Thymeleaf templates and custom CSS for a responsive, intuitive user interface that works seamlessly across devices.

9. Database Integration

Utilizes MySQL with Flyway for database migrations, ensuring data integrity and version control.

10. Advanced Concurrency and Transactions

Transactional operations for data consistency, with proper handling of concurrent user interactions.

                           Core Concepts and Design

1. Layered Architecture

The application follows a clean, layered architecture with distinct separation of concerns:
- Controller Layer: Handles HTTP requests and responses, delegating business logic to services.
- Service Layer: Contains business logic, with implementations providing concrete functionality.
- Repository Layer: Manages data access using Spring Data JPA.
- Entity Layer: JPA entities representing database tables with proper relationships.

2. Dependency Injection and Inversion of Control

Spring's IoC container manages dependencies, promoting loose coupling and testability. Services are injected into controllers and other components as needed.

3. Model-View-Controller (MVC) Pattern

Spring MVC framework separates the application into Model (entities and DTOs), View (Thymeleaf templates), and Controller (request handling) components.

4. Security Design

Spring Security provides authentication and authorization. Passwords are securely encoded using BCrypt. Role-based access ensures users can only perform actions appropriate to their permissions.

5. Data Transfer Objects (DTOs)

DTOs like `UserFormDto` and `UserDto` facilitate data transfer between layers, preventing direct exposure of entity internals and supporting form binding.

6. Transaction Management

Spring's `@Transactional` annotation ensures database operations are atomic, consistent, and isolated, preventing data corruption in multi-step operations.

7. Exception Handling

Runtime exceptions are thrown for invalid operations (e.g., duplicate usernames, missing required fields), with proper error propagation to the UI.

                           Project Structure
The project is structured as follows:

.
├── .gitattributes
├── .gitignore
├── mvnw
├── mvnw.cmd
├── nbactions.xml
├── pom.xml
├── ReadMe.md
└── src/
    ├── main/
    │   ├── java/com/school/sas/
    │   │   ├── SasApplication.java              # Main Spring Boot application class
    │   │   ├── DataInitializer.java             # Database initialization component
    │   │   ├── config/
    │   │   │   ├── CustomLoginSuccessHandler.java # Custom login redirect logic
    │   │   │   └── SecurityConfig.java          # Spring Security configuration
    │   │   ├── controller/
    │   │   │   ├── AdminController.java         # Admin dashboard and management endpoints
    │   │   │   ├── AuthController.java          # Authentication endpoints
    │   │   │   ├── DashboardController.java     # General dashboard routing
    │   │   │   ├── StudentController.java       # Student-specific endpoints
    │   │   │   └── TeacherController.java       # Teacher-specific endpoints
    │   │   ├── dto/
    │   │   │   ├── UserDto.java                 # User data transfer object
    │   │   │   └── UserFormDto.java             # User form data transfer object
    │   │   ├── entity/
    │   │   │   ├── Attendance.java              # Attendance entity
    │   │   │   ├── AttendanceStatus.java        # Attendance status enum
    │   │   │   ├── Enrollment.java              # Student-subject enrollment entity
    │   │   │   ├── Role.java                    # User role entity
    │   │   │   ├── SchoolClass.java             # School class entity
    │   │   │   ├── Subject.java                 # Subject entity
    │   │   │   ├── SubjectCategory.java         # Subject category enum
    │   │   │   ├── User.java                    # User entity
    │   │   │   └── UserType.java                # User type enum
    │   │   ├── repository/
    │   │   │   ├── AttendanceRepository.java    # Attendance data access
    │   │   │   ├── EnrollmentRepository.java    # Enrollment data access
    │   │   │   ├── RoleRepository.java          # Role data access
    │   │   │   ├── SchoolClassRepository.java   # School class data access
    │   │   │   ├── SubjectRepository.java       # Subject data access
    │   │   │   └── UserRepository.java          # User data access
    │   │   ├── service/
    │   │   │   ├── AttendanceService.java       # Attendance business logic
    │   │   │   ├── CustomUserDetailsService.java # User details for Spring Security
    │   │   │   ├── EnrollmentService.java       # Enrollment business logic
    │   │   │   ├── SchoolClassService.java      # School class business logic
    │   │   │   ├── StudentService.java          # Student-specific business logic
    │   │   │   ├── SubjectService.java          # Subject business logic
    │   │   │   ├── TeacherService.java          # Teacher-specific business logic
    │   │   │   └── UserService.java             # General user business logic
    │   │   └── service/impl/
    │   │       └── UserServiceImpl.java         # User service implementation
    │   └── resources/
    │       ├── application.properties           # Application configuration
    │       ├── static/css/
    │       │   ├── responsive.css               # Responsive design styles
    │       │   └── styles.css                   # Main application styles
    │       └── templates/
    │           ├── login.html                   # Login page
    │           ├── reset-password.html          # Password reset page
    │           ├── admin/                       # Admin-specific templates
    │           ├── fragments/                   # Reusable template fragments
    │           ├── student/                     # Student-specific templates
    │           └── teacher/                     # Teacher-specific templates
    └── test/java/com/school/sas/
        ├── SasApplicationTests.java             # Main application tests
        ├── StudentControllerTest.java           # Student controller tests
        ├── TeacherControllerTest.java           # Teacher controller tests
        └── UserServiceTest.java                 # User service tests

                           Getting Started 

Few Prerequisites Are:
Java 17 or higher
MySQL 8.0 or higher
Maven 3.8 or higher

Installation
1. Clone the repository to your local machine.
2. Ensure MySQL is running and create a database named `sas_db`.
3. Update the database credentials in `src/main/resources/application.properties` if necessary.
4. Navigate to the project root directory.
5. Run `mvn clean install` to build the project and download dependencies.
6. Run `mvn spring-boot:run` to start the application.

The application will be accessible at `http://localhost:8080`.

                    Dependencies

Java 17+
Spring Boot 3.5.6
Spring Boot Starter Data JPA
Spring Boot Starter Security
Spring Boot Starter Web
Spring Boot Starter Thymeleaf
Flyway MySQL
MySQL Connector/J
Microsoft SQL Server JDBC Driver
Lombok
Spring Boot Starter Test
Spring Security Test

                            Usage

1. User Roles and Dashboards
Upon successful login, users are redirected to role-specific dashboards:

2. Administrator Dashboard: Comprehensive overview with access to user management, class administration, subject management, and attendance oversight.

3. Teacher Dashboard: Access to assigned classes, attendance marking, grade entry, and subject management.

4. Student Dashboard: View personal profile, attendance records, enrolled subjects, and grades.

                    Key Functionalities

1. User Registration and Management: Admins can create new users (students/teachers) with detailed information including personal details, class assignments, and subject enrollments.

2. Class and Subject Assignment: Admins can manage school classes and subjects, assigning them to users as appropriate.

3. Attendance Management: Teachers can mark daily attendance for their classes; students and admins can view attendance records.

4. Grade Entry: Teachers can enter and update grades for students in their subjects.

5. Profile Management: Users can view and update their profiles (with appropriate restrictions based on role).

6. Password Reset: Secure password reset functionality for all users.

                        Database Initialization

The application uses Flyway for database migrations. On first run, it will automatically create the necessary tables and initialize basic data through the `DataInitializer` component.

                          File Formats Supported

The application primarily works with standard web and Java formats for a Spring Boot web application. It does not require specific file format for complex problems. All data is managed through the MySQL database with standard SQL operations.

1. Java (.java): Core application code, including Spring Boot controllers, services, entities, repositories, and test classes.

2. HTML (.html): Thymeleaf templates for web pages (login, dashboards, forms, etc.), with inline JavaScript for dynamic behavior.

3. CSS (.css): Stylesheets for responsive design and UI styling.

4. Properties (.properties): Configuration files for database and application settings.

5. XML (.xml): Maven POM file for project dependencies and build configuration, and NetBeans actions file.

6. Markdown (.md): Used to createREADME file for documentation.

                            Credits & References

This project was developed as part of the CS241 course and Assignment 2 requirements at The University of the South Pacific. It demonstrates practical application of software engineering principles, web development with Spring Boot, and database management in an educational context.
