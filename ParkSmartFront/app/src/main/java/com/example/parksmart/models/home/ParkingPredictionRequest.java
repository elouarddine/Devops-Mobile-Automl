package com.example.parksmart.models.home;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class ParkingPredictionRequest {
    @SerializedName("parking_id")
    private final String parkingId;
    private final String name;
    private final String address;
    private final Double latitude;
    private final Double longitude;
    @SerializedName("distance_m")
    private final Integer distanceMeters;
    private final Integer capacity;
    @SerializedName("current_free_places")
    private final Integer currentFreePlaces;
    @SerializedName("price_per_hour")
    private final Double pricePerHour;
    @SerializedName("arrival_option")
    private final String arrivalOption;
    @SerializedName("source_name")
    private final String sourceName;
    private final Map<String, Object> metadata;

    public ParkingPredictionRequest(ParkingUiModel item) {
        this.parkingId = item.getParkingId();
        this.name = item.getName();
        this.address = item.getAddress();
        this.latitude = item.getLatitude();
        this.longitude = item.getLongitude();
        this.distanceMeters = item.getDistanceMeters();
        this.capacity = item.getCapacity();
        this.currentFreePlaces = item.getCurrentFreePlaces();
        this.pricePerHour = item.getPricePerHour();
        this.arrivalOption = item.getArrivalOption();
        this.sourceName = item.getSourceName();
        this.metadata = new HashMap<>();
        this.metadata.put("details_text", item.getDetailsText());
    }
}
