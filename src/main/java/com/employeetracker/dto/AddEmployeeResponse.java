package com.employeetracker.dto;

public class AddEmployeeResponse {
    private Long id;
    private String employeeCode;
    private String username;
    private String fullName;
    private String email;
    private String message;

    public AddEmployeeResponse(Long id, String employeeCode, String username, String fullName,
                                String email, String message) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.message = message;
    }

    public Long getId() { return id; }
    public String getEmployeeCode() { return employeeCode; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getMessage() { return message; }
}
