# CS241 Assignments Repository

This repository contains the assignments submitted for CS241: Software Design and Implementation at The University of the South Pacific.

## Contents

- `A1/` — Assignment 1 materials.
- `A2/` — Assignment 2 materials, including the School Administration System (SAS) Spring Boot project.

## Assignment 2: School Administration System (SAS)

The A2 project is a Spring Boot web application built to support school administration tasks, including:

- Role-based access for administrators, teachers, and students
- User management and profile administration
- Class and subject assignments
- Attendance tracking and grade management
- Enrollment workflows and database initialization

### Technologies used

- Java 17+
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Security
- Thymeleaf
- MySQL
- Flyway
- Maven

## How to run Assignment 2

1. Change directory to the A2 project:
   ```bash
   cd A2/A2
   ```
2. Create a MySQL database named `sas_db`.
3. Update `src/main/resources/application.properties` with your database credentials.
4. Build the project:
   ```bash
   mvn clean install
   ```
5. Run the application:
   ```bash
   mvn spring-boot:run
   ```
6. Open the application in your browser at `http://localhost:8080`.

## Notes

- `A2/A2/Readme.md` contains detailed information specific to the School Administration System project.
- This repository is organized so that each assignment is kept in its own folder.

## License

This repository follows the project-level license configured in the A2 assignment.
