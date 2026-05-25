package com.studentnexus.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.studentnexus.config.RateLimiterConfig;
import com.studentnexus.model.StudentInfo;
import com.studentnexus.repository.StudentRepository;
import com.studentnexus.security.JwtUtil;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentController {

    @Autowired private StudentRepository repo;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RateLimiterConfig rateLimiterConfig;
    @Autowired private Cloudinary cloudinary;

    /** Create student — BCrypt-hashes the password before saving */
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody StudentInfo s) {
        if (repo.existsById(s.getUsn())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "USN already registered."));
        }
        s.setPassword(passwordEncoder.encode(s.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(s));
    }

    // REMOVED: unprotected GET /api/student (duplicate of /all)

    /** Teacher-only: list all students. Password is hidden via @JsonIgnore on model. */
    @GetMapping("/all")
    public List<StudentInfo> getAllStudents() {
        return repo.findAll();
    }

    /**
     * Login — rate-limited to 10 attempts/min per IP.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> creds,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        Bucket bucket = rateLimiterConfig.resolveBucket(ip);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many login attempts. Try again in a minute."));
        }

        String usn         = creds.get("usn");
        String rawPassword = creds.get("password");

        if (usn == null || usn.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "usn and password are required"));
        }

        return repo.findById(usn)
                .filter(s -> passwordEncoder.matches(rawPassword, s.getPassword()))
                .map(s -> {
                    String token = jwtUtil.generateToken(s.getUsn(), "STUDENT");
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "usn",   s.getUsn(),
                            "name",  s.getName()
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid USN or password")));
    }

    /** Dashboard — returns profile data for the logged-in student's USN */
    @GetMapping("/dashboard/{usn}")
    public ResponseEntity<?> dashboard(@PathVariable String usn) {
        return repo.findById(usn)
                .<ResponseEntity<?>>map(ResponseEntity::ok)   // password hidden by @JsonIgnore
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { repo.deleteById(id); }

    /**
     * Change password — verifies old password before setting new one.
     * PATCH /api/student/{usn}/password
     * Body: { "oldPassword": "...", "newPassword": "..." }
     */
    @PatchMapping("/{usn}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable String usn,
            @RequestBody Map<String, String> body) {

        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");

        if (oldPwd == null || newPwd == null || newPwd.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "oldPassword and newPassword (min 6 chars) are required"));
        }

        return repo.findById(usn)
                .map(s -> {
                    if (!passwordEncoder.matches(oldPwd, s.getPassword())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "Old password is incorrect"));
                    }
                    s.setPassword(passwordEncoder.encode(newPwd));
                    repo.save(s);
                    return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Upload profile photo to Cloudinary (safe for Railway — no local filesystem).
     * POST /api/student/{usn}/upload
     */
    @PostMapping("/{usn}/upload")
    public ResponseEntity<?> uploadPhoto(
            @PathVariable String usn,
            @RequestParam("file") MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "student-nexus",
                            "public_id", "student_" + usn,
                            "overwrite", true
                    )
            );
            String url = (String) result.get("secure_url");
            StudentInfo s = repo.findById(usn).orElseThrow();
            s.setPhotoUrl(url);
            repo.save(s);
            return ResponseEntity.ok(Map.of("photoUrl", url));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
