package com.example.parksmart.models.home;

public class HistoryApiResponse {
    private String status;
    private HistoryData data;
    private ApiError error;

    public String getStatus() { return status; }
    public HistoryData getData() { return data; }
    public ApiError getError() { return error; }
}
