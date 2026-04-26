package com.example.parksmart.models.home;

public class SavedParkingsApiResponse {
    private String status;
    private SavedParkingsData data;
    private ApiError error;

    public String getStatus() { return status; }
    public SavedParkingsData getData() { return data; }
    public ApiError getError() { return error; }
}
