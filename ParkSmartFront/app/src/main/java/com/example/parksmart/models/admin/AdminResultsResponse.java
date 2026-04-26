package com.example.parksmart.models.admin;

import com.example.parksmart.models.home.ApiError;

public class AdminResultsResponse {
    private String status;
    private AdminResultsData data;
    private ApiError error;

    public String getStatus() { return status; }
    public AdminResultsData getData() { return data; }
    public ApiError getError() { return error; }
}
