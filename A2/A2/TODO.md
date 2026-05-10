# TODO: Fix Teacher Deletion Issue

## Problem
Deleting teacher function doesn't actually delete teacher. Deleting students works though.

## Analysis
- Student deletion removes attendance records and enrollments before deleting the user.
- Teacher deletion removes teacher assignment from subjects but does NOT remove the teacher from SchoolClass as formTeacher.
- If a teacher is assigned as formTeacher to a class, deleting the teacher leaves a dangling reference in SchoolClass.formTeacher.

## Solution
Update `deleteTeacherById` in `UserServiceImpl.java` to also remove the teacher from any SchoolClass where they are the formTeacher.

## Steps
1. In `deleteTeacherById` method, after removing teacher from subjects, check if the teacher is assigned to a class.
2. If yes, set the SchoolClass.formTeacher to null and save the SchoolClass.
3. Ensure the method has access to schoolClassRepository (it already does).

## Files to Edit
- A2/src/main/java/com/school/sas/service/impl/UserServiceImpl.java

## Testing
- Test deleting a teacher who is assigned as formTeacher to a class.
- Test deleting a teacher who is not assigned to any class.
- Ensure no foreign key constraint violations.
