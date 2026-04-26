package com.example.parksmart.models.home;

public class BasicApiResponse {
    private String status;
    private ApiError error;

    public String getStatus() { return status; }
    public ApiError getError() { return error; }
}
