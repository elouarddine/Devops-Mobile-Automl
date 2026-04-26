package com.example.parksmart.models.admin;

import com.example.parksmart.models.home.ApiError;

public class AdminJobsResponse {
    private String status;
    private AdminJobsData data;
    private ApiError error;

    public String getStatus() { return status; }
    public AdminJobsData getData() { return data; }
    public ApiError getError() { return error; }
}
