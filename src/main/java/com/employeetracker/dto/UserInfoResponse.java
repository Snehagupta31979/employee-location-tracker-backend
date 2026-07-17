package com.employeetracker.dto;

public class UserInfoResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String role;

    public UserInfoResponse() {
    }

    public UserInfoResponse(Long id, String username, String fullName, String email, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String role;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public UserInfoResponse build() {
            return new UserInfoResponse(id, username, fullName, email, role);
        }
    }
}
