package com.studentnexus.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "grades")
public class Grade {

    @Id
    private String id;

    private String usn;
    private int semester;
    private String subjectCode;
    private String subjectName;
    private int internalMarks;
    private int externalMarks;
    private int totalMarks;
    private String grade;
    private int credits;
    private double gradePoints;   // e.g. O=10, A+=9, A=8 … used for CGPA
}
