package com.example.parksmart.models.home;

import com.google.gson.annotations.SerializedName;

public class RecommendedParking {

    @SerializedName("parking_id")
    private String parkingId;
    private String reason;

    public String getParkingId() {
        return parkingId;
    }

    public String getReason() {
        return reason;
    }
}
