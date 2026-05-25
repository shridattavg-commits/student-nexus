package com.studentnexus.repository;

import com.studentnexus.model.TeacherInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeacherRepository extends MongoRepository<TeacherInfo, String> { }
