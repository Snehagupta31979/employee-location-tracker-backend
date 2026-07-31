package com.employeetracker.dto;

import java.time.LocalDate;

public class EmployeeProfileResponse {
    private Long id;
    private String employeeCode;
    private String fullName;
    private String email;
    private String role;
    private String status;
    private String department;
    private String designation;
    private String mobile;
    private String address;
    private LocalDate joiningDate;

    public EmployeeProfileResponse() {}

    public EmployeeProfileResponse(Long id, String employeeCode, String fullName, String email, String role,
                                    String status, String department, String designation, String mobile,
                                    String address, LocalDate joiningDate) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status;
        this.department = department;
        this.designation = designation;
        this.mobile = mobile;
        this.address = address;
        this.joiningDate = joiningDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
}
