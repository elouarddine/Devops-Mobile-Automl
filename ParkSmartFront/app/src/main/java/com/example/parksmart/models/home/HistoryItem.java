package com.example.parksmart.models.home;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HistoryItem {
    @SerializedName("id")
    private Integer id;
    @SerializedName("searched_at")
    private String searchedAt;
    @SerializedName("destination_text")
    private String destinationText;
    @SerializedName("destination_lat")
    private Double destinationLat;
    @SerializedName("destination_lon")
    private Double destinationLon;
    @SerializedName("arrival_option")
    private String arrivalOption;
    @SerializedName("recommended_parking")
    private String recommendedParking;
    private List<ParkingResult> results;

    public Integer getId() { return id; }
    public String getSearchedAt() { return searchedAt; }
    public String getDestinationText() { return destinationText; }
    public Double getDestinationLat() { return destinationLat; }
    public Double getDestinationLon() { return destinationLon; }
    public String getArrivalOption() { return arrivalOption; }
    public String getRecommendedParking() { return recommendedParking; }
    public List<ParkingResult> getResults() { return results; }
}
