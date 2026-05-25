package com.studentnexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "student")
public class StudentInfo {

    @Id
    @NotBlank(message = "USN cannot be empty")
    private String usn;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // still accepted on POST
    @NotBlank(message = "Password cannot be empty")
    private String password;

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNo;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String emailId;

    private String photoUrl;
    private String portFolioUrl;
    private String branch;
    private Integer semester;
}
