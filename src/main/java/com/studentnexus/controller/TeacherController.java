package com.studentnexus.controller;

import com.studentnexus.config.RateLimiterConfig;
import com.studentnexus.model.TeacherInfo;
import com.studentnexus.repository.TeacherRepository;
import com.studentnexus.security.JwtUtil;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin
public class TeacherController {

    @Autowired private TeacherRepository repo;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RateLimiterConfig rateLimiterConfig;

    @Value("${app.teacher.invite-code}")
    private String validInviteCode;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String inviteCode = body.get("inviteCode");
        String teacherId  = body.get("teacherId");
        String name       = body.get("name");
        String email      = body.get("emailId");
        String password   = body.get("password");
        String department = body.get("department");

        // Basic validation
        if (teacherId == null || teacherId.isBlank() ||
            name == null      || name.isBlank()      ||
            email == null     || email.isBlank()     ||
            password == null  || password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "teacherId, name, emailId, and password (min 6 chars) are required"));
        }

        if (!validInviteCode.equals(inviteCode)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Invalid invite code."));
        }
        if (repo.existsById(teacherId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Teacher ID already registered."));
        }

        TeacherInfo t = new TeacherInfo();
        t.setTeacherId(teacherId);
        t.setName(name);
        t.setEmailId(email);
        t.setPassword(passwordEncoder.encode(password));
        t.setDepartment(department);
        repo.save(t);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Teacher registered successfully.", "teacherId", t.getTeacherId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> creds,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        Bucket bucket = rateLimiterConfig.resolveBucket("teacher:" + ip);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many login attempts. Try again in a minute."));
        }

        String id  = creds.get("teacherId");
        String pwd = creds.get("password");

        if (id == null || id.isBlank() || pwd == null || pwd.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "teacherId and password are required"));
        }

        return repo.findById(id)
                .filter(t -> passwordEncoder.matches(pwd, t.getPassword()))
                .map(t -> {
                    String token = jwtUtil.generateToken(t.getTeacherId(), "TEACHER");
                    return ResponseEntity.ok(Map.of(
                            "token",     token,
                            "teacherId", t.getTeacherId(),
                            "name",      t.getName()
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid Teacher ID or password.")));
    }
}
