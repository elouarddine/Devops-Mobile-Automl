package com.example.parksmart.models.home;

import com.google.gson.annotations.SerializedName;

public class SavedParkingItem {
    @SerializedName("parking_id")
    private String parkingId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    @SerializedName("distance_m")
    private Integer distanceMeters;
    private Integer capacity;
    @SerializedName("current_free_places")
    private Integer currentFreePlaces;
    @SerializedName("predicted_free_places")
    private Integer predictedFreePlaces;
    @SerializedName("price_per_hour")
    private Double pricePerHour;
    @SerializedName("arrival_option")
    private String arrivalOption;
    @SerializedName("saved_at")
    private String savedAt;

    public String getParkingId() { return parkingId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Integer getDistanceMeters() { return distanceMeters; }
    public Integer getCapacity() { return capacity; }
    public Integer getCurrentFreePlaces() { return currentFreePlaces; }
    public Integer getPredictedFreePlaces() { return predictedFreePlaces; }
    public Double getPricePerHour() { return pricePerHour; }
    public String getArrivalOption() { return arrivalOption; }
    public String getSavedAt() { return savedAt; }
}
