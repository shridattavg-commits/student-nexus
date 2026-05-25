package com.studentnexus.controller;

import com.studentnexus.model.Grade;
import com.studentnexus.repository.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin
public class GradeController {

    @Autowired private GradeRepository gradeRepo;

    /** Returns grades grouped by semester for the given USN */
    @GetMapping("/{usn}")
    public Map<Integer, List<Grade>> getGrades(@PathVariable String usn) {
        List<Grade> grades = gradeRepo.findByUsnOrderBySemesterAsc(usn);
        return grades.stream()
                .collect(Collectors.groupingBy(Grade::getSemester,
                         TreeMap::new, Collectors.toList()));
    }

    /**
     * CGPA calculation — weighted GPA across all semesters.
     * GET /api/grades/{usn}/cgpa
     * Returns: { cgpa: 8.76, semesterGpa: { 1: 8.5, 2: 9.0, ... } }
     */
    @GetMapping("/{usn}/cgpa")
    public ResponseEntity<?> getCgpa(@PathVariable String usn) {
        List<Grade> grades = gradeRepo.findByUsnOrderBySemesterAsc(usn);

        if (grades.isEmpty()) {
            return ResponseEntity.ok(Map.of("cgpa", 0.0, "semesterGpa", Map.of()));
        }

        // Per-semester GPA: sum(credits * gradePoints) / sum(credits)
        Map<Integer, Double> semesterGpa = grades.stream()
                .collect(Collectors.groupingBy(
                        Grade::getSemester,
                        TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                semGrades -> {
                                    double totalWeighted = semGrades.stream()
                                            .mapToDouble(g -> g.getCredits() * g.getGradePoints()).sum();
                                    double totalCredits  = semGrades.stream()
                                            .mapToDouble(Grade::getCredits).sum();
                                    return totalCredits == 0 ? 0.0
                                            : Math.round((totalWeighted / totalCredits) * 100.0) / 100.0;
                                }
                        )
                ));

        // Overall CGPA across all semesters
        double totalWeighted = grades.stream()
                .mapToDouble(g -> g.getCredits() * g.getGradePoints()).sum();
        double totalCredits  = grades.stream().mapToDouble(Grade::getCredits).sum();
        double cgpa = totalCredits == 0 ? 0.0
                : Math.round((totalWeighted / totalCredits) * 100.0) / 100.0;

        return ResponseEntity.ok(Map.of("cgpa", cgpa, "semesterGpa", semesterGpa));
    }

    // ── Teacher-only grade CRUD ────────────────────────────────────────────

    /** POST /api/grades — add a grade record (teacher only) */
    @PostMapping
    public ResponseEntity<?> addGrade(@RequestBody Grade grade) {
        if (grade.getUsn() == null || grade.getSubjectCode() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "usn and subjectCode are required"));
        }
        return ResponseEntity.ok(gradeRepo.save(grade));
    }

    /**
     * PUT /api/grades/{id} — update a grade record (teacher only).
     * Accepts the same body as POST; replaces the record.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGrade(@PathVariable String id, @RequestBody Grade updated) {
        return gradeRepo.findById(id)
                .map(existing -> {
                    updated.setId(id);           // preserve ID
                    return ResponseEntity.ok(gradeRepo.save(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/grades/{id} — remove a grade record (teacher only) */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGrade(@PathVariable String id) {
        if (!gradeRepo.existsById(id)) return ResponseEntity.notFound().build();
        gradeRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Grade deleted"));
    }
}
