package com.example.parksmart.models.home;

public class AuthMeResponse {
    private String status;
    private UserProfile data;
    private ApiError error;

    public String getStatus() { return status; }
    public UserProfile getData() { return data; }
    public ApiError getError() { return error; }
}
