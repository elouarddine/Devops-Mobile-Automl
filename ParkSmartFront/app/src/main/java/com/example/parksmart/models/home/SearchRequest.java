package com.example.parksmart.models.home;

import com.google.gson.annotations.SerializedName;

public class SearchRequest {

    @SerializedName("destination_text")
    private final String destinationText;

    @SerializedName("destination_lat")
    private final Double destinationLat;

    @SerializedName("destination_lon")
    private final Double destinationLon;

    @SerializedName("arrival_option")
    private final String arrivalOption;

    @SerializedName("radius_m")
    private final int radiusMeters;

    public SearchRequest(String destinationText) {
        this(destinationText, null, null, "plus_30", 2000);
    }

    public SearchRequest(String destinationText, Double destinationLat, Double destinationLon, String arrivalOption, int radiusMeters) {
        this.destinationText = destinationText;
        this.destinationLat = destinationLat;
        this.destinationLon = destinationLon;
        this.arrivalOption = arrivalOption;
        this.radiusMeters = radiusMeters;
    }
}
