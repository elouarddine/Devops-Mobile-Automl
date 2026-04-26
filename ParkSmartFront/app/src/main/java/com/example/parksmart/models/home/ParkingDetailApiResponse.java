package com.example.parksmart.models.home;

public class ParkingDetailApiResponse {
    private String status;
    private ParkingDetail data;
    private ApiError error;

    public String getStatus() { return status; }
    public ParkingDetail getData() { return data; }
    public ApiError getError() { return error; }
}
