# CS241 Assignment Two: School Administration System (SAS)

![Java](https://img.shields.io/badge/Java-17+-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-orange.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

## Contributors
- Ranveer Singh (S11230141)
- Ronil Prasad (S11231541)
- Shivan Prasad (S11231502)
- Mohammed Suhail (S11230995)
- Mohammed Afeef (S11229568)

## Course
- CS241 - Software Design and Implementation
- The University of the South Pacific
- Date: 14 October 2025

## Overview

School Administration System (SAS) is a Spring Boot web application built to support secure and efficient management of school operations. The system handles user accounts for administrators, teachers, and students, along with class and subject assignment, attendance tracking, grade management, and enrollment workflows.

## Table of Contents
- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Usage](#usage)
- [Dependencies](#dependencies)
- [Testing](#testing)
- [Credits](#credits)
- [License](#license)

## Features

- Role-based access for Administrators, Teachers, and Students
- Secure authentication and authorization with Spring Security
- Admin user management for students and teachers
- Class and subject administration with dynamic assignments
- Attendance tracking and reporting
- Grade entry and student performance viewing
- Enrollment management with automated grade initialization
- Responsive UI using Thymeleaf and custom CSS
- MySQL database integration with Flyway migrations
- Transactional operations to preserve data consistency

## Architecture

SAS follows a layered architecture inspired by Spring Boot best practices:

- **Controller Layer**: Handles HTTP requests and delegates business logic
- **Service Layer**: Implements core application workflows
- **Repository Layer**: Manages persistence using Spring Data JPA
- **Entity Layer**: Defines domain objects and relationships
- **Security Layer**: Uses Spring Security for authentication, authorization, and role-based routing

## Project Structure

```
A2/A2/
├── .gitattributes
├── .gitignore
├── mvnw
├── mvnw.cmd
├── nbactions.xml
├── pom.xml
├── Readme.md
├── src/
│   ├── main/
│   │   ├── java/com/school/sas/
│   │   │   ├── SasApplication.java
│   │   │   ├── DataInitializer.java
│   │   │   ├── config/
│   │   │   │   ├── CustomLoginSuccessHandler.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── StudentController.java
│   │   │   │   └── TeacherController.java
│   │   │   ├── dto/
│   │   │   │   ├── UserDto.java
│   │   │   │   └── UserFormDto.java
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── service/impl/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/css/
│   │       └── templates/
│   └── test/java/com/school/sas/
└── README.md
```

## Getting Started

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.8 or higher

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/ronilprasad8/CS241.git
   cd CS241/Assignments/A2/A2
   ```
2. Create a MySQL database named `sas_db`.
3. Update database credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/sas_db
   spring.datasource.username=<your-username>
   spring.datasource.password=<your-password>
   ```
4. Build the project:
   ```bash
   mvn clean install
   ```
5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Access
Open your browser at:

```text
http://localhost:8080
```

## Configuration

The main application configuration is located in `src/main/resources/application.properties`.

Important settings:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.jpa.hibernate.ddl-auto`

## Usage

### User Roles
- **Administrator**: Manage users, classes, subjects, attendance, and enrollment
- **Teacher**: Mark attendance, enter grades, and manage assigned classes
- **Student**: View dashboard, attendance, enrolled subjects, and grades

### Key Workflows
- Admins create and manage student/teacher accounts
- Teachers record attendance and grades
- Students review their academic progress
- Shared dashboards adapt based on user role

## Dependencies

Core dependencies used in this assignment:
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Thymeleaf
- Flyway Core
- MySQL Connector/J
- Lombok
- Spring Boot Starter Test
- Spring Security Test

## Testing

Run the test suite with:

```bash
mvn test
```

## Credits

Developed for CS241 at The University of the South Pacific by:
- Ranveer Singh
- Ronil Prasad
- Shivan Prasad
- Mohammed Suhail
- Mohammed Afeef

## License

This project is released under the MIT License.
