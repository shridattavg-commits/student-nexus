package com.studentnexus.config;

import com.studentnexus.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Public: auth endpoints ──
                .requestMatchers("/api/student/login").permitAll()
                .requestMatchers("/api/student").permitAll()          // POST create student
                .requestMatchers("/api/teacher/login").permitAll()
                .requestMatchers("/api/teacher/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/student/*/upload").authenticated()
                // ── Public: static files ──
                .requestMatchers("/uploads/**", "/*.html", "/", "/index.html",
                                 "/login.html", "/dashboard.html", "/crud.html").permitAll()

                // ── Teacher-only write endpoints ──
                .requestMatchers(HttpMethod.POST,   "/api/attendance").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PATCH,  "/api/attendance/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/attendance/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.GET,    "/api/student/all").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/student/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.POST,   "/api/grades").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PUT,    "/api/grades/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/grades/**").hasRole("TEACHER")

                // ── Everything else requires any valid JWT ──
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
