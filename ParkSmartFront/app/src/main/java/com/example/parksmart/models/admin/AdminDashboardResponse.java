package com.example.parksmart.models.admin;

import com.example.parksmart.models.home.ApiError;

public class AdminDashboardResponse {
    private String status;
    private AdminDashboardData data;
    private ApiError error;

    public String getStatus() { return status; }
    public AdminDashboardData getData() { return data; }
    public ApiError getError() { return error; }
}
