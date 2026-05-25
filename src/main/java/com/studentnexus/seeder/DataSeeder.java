package com.studentnexus.seeder;

import com.studentnexus.model.Attendance;
import com.studentnexus.model.Grade;
import com.studentnexus.model.StudentInfo;
import com.studentnexus.repository.AttendanceRepository;
import com.studentnexus.repository.GradeRepository;
import com.studentnexus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds sample data on first run (only if collections are empty).
 * Sample USN: 1RV22CS001  |  Password: password123
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private StudentRepository studentRepo;
    @Autowired private GradeRepository gradeRepo;
    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedStudents();
        seedGrades();
        seedAttendance();
    }

    private void seedStudents() {
        if (studentRepo.count() > 0) return;

        StudentInfo s = new StudentInfo();
        s.setUsn("1RV22CS001");
        s.setPassword(passwordEncoder.encode("password123"));
        s.setName("Arjun Sharma");
        s.setEmailId("arjun.sharma@rvce.edu.in");
        s.setPhoneNo("9876543210");
        s.setBranch("Computer Science and Engineering");
        s.setSemester(4);
        s.setPortFolioUrl("https://arjunsharma.dev");
        s.setPhotoUrl("/uploads/pokemon-aesthetic-goldb8hz1kbvvd50.jpg");
        studentRepo.save(s);

        System.out.println("[DataSeeder] Seeded 1 student: USN=1RV22CS001, Password=password123");
    }

    private void seedGrades() {
        if (gradeRepo.count() > 0) return;

        String usn = "1RV22CS001";

        // Semester 3
        gradeRepo.save(grade(usn, 3, "21CS31", "Data Structures",        38, 72, "A", 4));
        gradeRepo.save(grade(usn, 3, "21CS32", "Digital Design",         36, 68, "B+", 3));
        gradeRepo.save(grade(usn, 3, "21MA31", "Engineering Mathematics", 40, 78, "A+", 4));
        gradeRepo.save(grade(usn, 3, "21CS33", "Computer Organization",   35, 65, "B",  3));

        // Semester 4
        gradeRepo.save(grade(usn, 4, "21CS41", "Analysis of Algorithms",  39, 75, "A",  4));
        gradeRepo.save(grade(usn, 4, "21CS42", "Microprocessors",         37, 70, "A",  3));
        gradeRepo.save(grade(usn, 4, "21CS43", "Operating Systems",       40, 80, "O",  4));
        gradeRepo.save(grade(usn, 4, "21CS44", "Database Management",     38, 76, "A+", 3));

        System.out.println("[DataSeeder] Seeded grades for 1RV22CS001");
    }

    private Grade grade(String usn, int sem, String code, String name,
                        int internal, int external, String g, int credits) {
        Grade gr = new Grade();
        gr.setUsn(usn);
        gr.setSemester(sem);
        gr.setSubjectCode(code);
        gr.setSubjectName(name);
        gr.setInternalMarks(internal);
        gr.setExternalMarks(external);
        gr.setTotalMarks(internal + external);
        gr.setGrade(g);
        gr.setCredits(credits);
        return gr;
    }

    private void seedAttendance() {
        if (attendanceRepo.count() > 0) return;

        String usn = "1RV22CS001";

        // Semester 3
        attendanceRepo.save(att(usn, 3, "21CS31", "Data Structures",         52, 60));
        attendanceRepo.save(att(usn, 3, "21CS32", "Digital Design",           40, 55));  // WARNING
        attendanceRepo.save(att(usn, 3, "21MA31", "Engineering Mathematics",  58, 60));
        attendanceRepo.save(att(usn, 3, "21CS33", "Computer Organization",    30, 55));  // DETAINED

        // Semester 4
        attendanceRepo.save(att(usn, 4, "21CS41", "Analysis of Algorithms",   42, 50));
        attendanceRepo.save(att(usn, 4, "21CS42", "Microprocessors",          38, 50));  // WARNING
        attendanceRepo.save(att(usn, 4, "21CS43", "Operating Systems",        48, 52));
        attendanceRepo.save(att(usn, 4, "21CS44", "Database Management",      50, 55));

        System.out.println("[DataSeeder] Seeded attendance for 1RV22CS001");
    }

    private Attendance att(String usn, int sem, String code, String name,
                           int attended, int total) {
        Attendance a = new Attendance();
        a.setUsn(usn);
        a.setSemester(sem);
        a.setSubjectCode(code);
        a.setSubjectName(name);
        a.setClassesAttended(attended);
        a.setTotalClasses(total);
        return a;
    }
}
