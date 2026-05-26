package com.studentnexus.controller;

import com.studentnexus.model.Grade;
import com.studentnexus.repository.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin
public class GradeController {

    @Autowired
    private GradeRepository gradeRepo;

    /* ─────────────────────────────────────────────────────────────
       GET ALL GRADES GROUPED BY SEMESTER
       GET /api/grades/{usn}
    ───────────────────────────────────────────────────────────── */

    @GetMapping("/{usn}")
    public Map<Integer, List<Grade>> getGrades(@PathVariable String usn) {

        List<Grade> grades = gradeRepo.findByUsnOrderBySemesterAsc(usn);

        return grades.stream()
                .collect(Collectors.groupingBy(
                        Grade::getSemester,
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

    /* ─────────────────────────────────────────────────────────────
       CGPA CALCULATION
       GET /api/grades/{usn}/cgpa
    ───────────────────────────────────────────────────────────── */

    @GetMapping("/{usn}/cgpa")
    public ResponseEntity<?> getCgpa(@PathVariable String usn) {

        List<Grade> grades = gradeRepo.findByUsnOrderBySemesterAsc(usn);

        if (grades.isEmpty()) {

            return ResponseEntity.ok(
                    Map.of(
                            "cgpa", 0.0,
                            "semesterGpa", Map.of()
                    )
            );
        }

        Map<Integer, Double> semesterGpa = grades.stream()
                .collect(Collectors.groupingBy(
                        Grade::getSemester,
                        TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                semGrades -> {

                                    double totalWeighted = semGrades.stream()
                                            .mapToDouble(g ->
                                                    g.getCredits() * g.getGradePoints())
                                            .sum();

                                    double totalCredits = semGrades.stream()
                                            .mapToDouble(Grade::getCredits)
                                            .sum();

                                    return totalCredits == 0
                                            ? 0.0
                                            : Math.round(
                                            (totalWeighted / totalCredits) * 100.0
                                    ) / 100.0;
                                }
                        )
                ));

        double totalWeighted = grades.stream()
                .mapToDouble(g ->
                        g.getCredits() * g.getGradePoints())
                .sum();

        double totalCredits = grades.stream()
                .mapToDouble(Grade::getCredits)
                .sum();

        double cgpa = totalCredits == 0
                ? 0.0
                : Math.round(
                (totalWeighted / totalCredits) * 100.0
        ) / 100.0;

        return ResponseEntity.ok(
                Map.of(
                        "cgpa", cgpa,
                        "semesterGpa", semesterGpa
                )
        );
    }

    /* ─────────────────────────────────────────────────────────────
       ADD GRADE SUBJECT
       POST /api/grades
    ───────────────────────────────────────────────────────────── */

    @PostMapping
    public ResponseEntity<?> addGrade(@RequestBody Grade grade) {

        if (grade.getUsn() == null ||
                grade.getSubjectCode() == null ||
                grade.getSubjectName() == null) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "usn, subjectCode and subjectName are required"
                    )
            );
        }

        Grade saved = gradeRepo.save(grade);

        return ResponseEntity.ok(saved);
    }

    /* ─────────────────────────────────────────────────────────────
       UPDATE GRADES
       PATCH /api/grades/{id}
    ───────────────────────────────────────────────────────────── */

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateGrade(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload) {

        Optional<Grade> optionalGrade = gradeRepo.findById(id);

        if (optionalGrade.isEmpty()) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "error",
                                    "Grade record not found"
                            )
                    );
        }

        Grade grade = optionalGrade.get();

        if (payload.containsKey("internalMarks")) {

            grade.setInternalMarks(
                    ((Number) payload.get("internalMarks")).intValue()
            );
        }

        if (payload.containsKey("externalMarks")) {

            grade.setExternalMarks(
                    ((Number) payload.get("externalMarks")).intValue()
            );
        }

        Grade saved = gradeRepo.save(grade);

        return ResponseEntity.ok(saved);
    }

    /* ─────────────────────────────────────────────────────────────
       DELETE GRADE
       DELETE /api/grades/{id}
    ───────────────────────────────────────────────────────────── */

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGrade(@PathVariable String id) {

        if (!gradeRepo.existsById(id)) {

            return ResponseEntity.notFound().build();
        }

        gradeRepo.deleteById(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Grade deleted successfully"
                )
        );
    }
}