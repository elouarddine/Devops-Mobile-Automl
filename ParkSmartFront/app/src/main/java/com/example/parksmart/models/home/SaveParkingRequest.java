package com.example.parksmart.models.home;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class SaveParkingRequest {
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
    @SerializedName("predicted_free_places")
    private final Integer predictedFreePlaces;
    @SerializedName("price_per_hour")
    private final Double pricePerHour;
    @SerializedName("arrival_option")
    private final String arrivalOption;
    private final Map<String, Object> metadata;

    public SaveParkingRequest(ParkingUiModel item) {
        this.parkingId = item.getParkingId();
        this.name = item.getName();
        this.address = item.getAddress();
        this.latitude = item.getLatitude();
        this.longitude = item.getLongitude();
        this.distanceMeters = item.getDistanceMeters();
        this.capacity = item.getCapacity();
        this.currentFreePlaces = item.getCurrentFreePlaces();
        this.predictedFreePlaces = item.getPredictedFreePlaces();
        this.pricePerHour = item.getPricePerHour();
        this.arrivalOption = item.getArrivalOption();
        this.metadata = new HashMap<>();
        this.metadata.put("details_text", item.getDetailsText());
    }
}
