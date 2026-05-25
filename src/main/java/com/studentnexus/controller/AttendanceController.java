package com.studentnexus.controller;

import com.studentnexus.model.Attendance;
import com.studentnexus.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin
public class AttendanceController {

    @Autowired private AttendanceRepository attendanceRepo;

    /** Returns attendance grouped by semester for the given USN */
    @GetMapping("/{usn}")
    public Map<Integer, List<Attendance>> getAttendance(@PathVariable String usn) {
        List<Attendance> records = attendanceRepo.findByUsnOrderBySemesterAsc(usn);
        return records.stream()
                .collect(Collectors.groupingBy(Attendance::getSemester,
                         TreeMap::new, Collectors.toList()));
    }

    /**
     * Teacher Portal — add a new attendance record for a student.
     * POST /api/attendance
     * Body: { "usn": "41", "semester": 1, "subjectCode": "CS101",
     *         "subjectName": "Mathematics", "classesAttended": 30, "totalClasses": 40 }
     */
    @PostMapping
    public ResponseEntity<?> addAttendance(@RequestBody Attendance attendance) {
        if (attendance.getUsn() == null || attendance.getSubjectCode() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "usn and subjectCode are required"));
        }
        Attendance saved = attendanceRepo.save(attendance);
        return ResponseEntity.ok(saved);
    }

    /**
     * Teacher Portal — update classesAttended and totalClasses for a specific record.
     * PATCH /api/attendance/{id}
     * Body: { "classesAttended": 45, "totalClasses": 52 }
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable String id,
            @RequestBody Map<String, Integer> updates) {

        return attendanceRepo.findById(id)
                .map(record -> {
                    if (updates.containsKey("classesAttended")) {
                        record.setClassesAttended(updates.get("classesAttended"));
                    }
                    if (updates.containsKey("totalClasses")) {
                        record.setTotalClasses(updates.get("totalClasses"));
                    }
                    Attendance saved = attendanceRepo.save(record);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Teacher Portal — delete an attendance record.
     * DELETE /api/attendance/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttendance(@PathVariable String id) {
        if (!attendanceRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        attendanceRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }
}
