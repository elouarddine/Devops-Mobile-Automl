package com.example.parksmart.models.home;

import com.google.gson.annotations.SerializedName;

public class SearchDestination {
    private String text;
    private Double latitude;
    @SerializedName("longitude")
    private Double longitude;

    public String getText() { return text; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}
