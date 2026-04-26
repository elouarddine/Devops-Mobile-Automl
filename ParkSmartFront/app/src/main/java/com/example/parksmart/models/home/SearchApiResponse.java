package com.example.parksmart.models.home;

public class SearchApiResponse {

    private String status;
    private SearchData data;
    private ApiError error;

    public String getStatus() {
        return status;
    }

    public SearchData getData() {
        return data;
    }

    public ApiError getError() {
        return error;
    }
}
