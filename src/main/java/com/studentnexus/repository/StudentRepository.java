package com.studentnexus.repository;

import com.studentnexus.model.StudentInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends MongoRepository<StudentInfo, String> {
}