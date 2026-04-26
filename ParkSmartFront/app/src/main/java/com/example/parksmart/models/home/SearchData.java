package com.example.parksmart.models.home;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchData {

    @SerializedName("arrival_option")
    private String arrivalOption;

    private SearchDestination destination;
    private RecommendedParking recommended;
    private List<ParkingResult> results;

    public String getArrivalOption() {
        return arrivalOption;
    }

    public SearchDestination getDestination() {
        return destination;
    }

    public RecommendedParking getRecommended() {
        return recommended;
    }

    public List<ParkingResult> getResults() {
        return results;
    }
}
