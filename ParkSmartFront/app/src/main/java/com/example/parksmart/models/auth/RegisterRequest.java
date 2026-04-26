package com.example.parksmart.models.auth;

public class RegisterRequest {

    private final String fullName;
    private final String email;
    private final String password;
    private final String role;

    public RegisterRequest(String fullName, String email, String password, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}