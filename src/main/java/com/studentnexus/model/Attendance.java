package com.studentnexus.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "attendance")
public class Attendance {

    @Id
    private String id;

    private String usn;
    private int semester;
    private String subjectCode;
    private String subjectName;
    private int classesAttended;
    private int totalClasses;

    // Computed at runtime — not stored
    public double getPercentage() {
        if (totalClasses == 0) return 0;
        return Math.round((classesAttended * 100.0 / totalClasses) * 10.0) / 10.0;
    }

    public String getStatus() {
        double pct = getPercentage();
        if (pct >= 75) return "SAFE";
        if (pct >= 60) return "WARNING";
        return "DETAINED";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsn() { return usn; }
    public void setUsn(String usn) { this.usn = usn; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public int getClassesAttended() { return classesAttended; }
    public void setClassesAttended(int classesAttended) { this.classesAttended = classesAttended; }
    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
}
