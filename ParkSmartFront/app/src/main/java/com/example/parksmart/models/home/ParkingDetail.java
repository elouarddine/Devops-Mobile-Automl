package com.example.parksmart.models.home;

import com.google.gson.annotations.SerializedName;

public class ParkingDetail {
    @SerializedName("parking_id")
    private String parkingId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer capacity;
    @SerializedName("current_free_places")
    private Integer currentFreePlaces;
    @SerializedName("predicted_free_places")
    private Integer predictedFreePlaces;
    @SerializedName("price_per_hour")
    private Double pricePerHour;
    @SerializedName("source_name")
    private String sourceName;
    @SerializedName("distance_m")
    private Integer distanceMeters;
    @SerializedName("arrival_option")
    private String arrivalOption;
    @SerializedName("prediction_fallback_used")
    private Boolean predictionFallbackUsed;
    @SerializedName("prediction_reason")
    private String predictionReason;

    public String getParkingId() { return parkingId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Integer getCapacity() { return capacity; }
    public Integer getCurrentFreePlaces() { return currentFreePlaces; }
    public Integer getPredictedFreePlaces() { return predictedFreePlaces; }
    public Double getPricePerHour() { return pricePerHour; }
    public String getSourceName() { return sourceName; }
    public Integer getDistanceMeters() { return distanceMeters; }
    public String getArrivalOption() { return arrivalOption; }
    public Boolean getPredictionFallbackUsed() { return predictionFallbackUsed; }
    public String getPredictionReason() { return predictionReason; }
}
