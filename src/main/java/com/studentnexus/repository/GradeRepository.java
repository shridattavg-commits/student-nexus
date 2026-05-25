package com.studentnexus.repository;

import com.studentnexus.model.Grade;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GradeRepository extends MongoRepository<Grade, String> {
    List<Grade> findByUsnOrderBySemesterAsc(String usn);
}
