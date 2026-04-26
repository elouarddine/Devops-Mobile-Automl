package com.example.parksmart.models.home;

public class ParkingUiModel {

    private String parkingId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer distanceMeters;
    private String distanceText;
    private String availabilityText;
    private String predictionText;
    private String detailsText;
    private Integer currentFreePlaces;
    private Integer predictedFreePlaces;
    private Integer capacity;
    private Double pricePerHour;
    private String arrivalOption;
    private String sourceName;
    private boolean saved;
    private boolean recommended;

    public ParkingUiModel() {}

    public ParkingUiModel(String parkingId, String name, String address, Double latitude, Double longitude, Integer distanceMeters, String distanceText, String availabilityText, String predictionText, String detailsText, Integer currentFreePlaces, Integer predictedFreePlaces, Integer capacity, Double pricePerHour, String arrivalOption, String sourceName, boolean saved, boolean recommended) {
        this.parkingId = parkingId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceMeters = distanceMeters;
        this.distanceText = distanceText;
        this.availabilityText = availabilityText;
        this.predictionText = predictionText;
        this.detailsText = detailsText;
        this.currentFreePlaces = currentFreePlaces;
        this.predictedFreePlaces = predictedFreePlaces;
        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
        this.arrivalOption = arrivalOption;
        this.sourceName = sourceName;
        this.saved = saved;
        this.recommended = recommended;
    }

    public String getParkingId() { return parkingId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Integer getDistanceMeters() { return distanceMeters; }
    public String getDistanceText() { return distanceText; }
    public String getAvailabilityText() { return availabilityText; }
    public String getPredictionText() { return predictionText; }
    public String getDetailsText() { return detailsText; }
    public Integer getCurrentFreePlaces() { return currentFreePlaces; }
    public Integer getPredictedFreePlaces() { return predictedFreePlaces; }
    public Integer getCapacity() { return capacity; }
    public Double getPricePerHour() { return pricePerHour; }
    public String getArrivalOption() { return arrivalOption; }
    public String getSourceName() { return sourceName; }
    public boolean isSaved() { return saved; }
    public boolean isRecommended() { return recommended; }

    public void setSaved(boolean saved) { this.saved = saved; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
    public void setDetailsText(String detailsText) { this.detailsText = detailsText; }
    public void setPredictionText(String predictionText) { this.predictionText = predictionText; }
    public void setAvailabilityText(String availabilityText) { this.availabilityText = availabilityText; }
    public void setCurrentFreePlaces(Integer currentFreePlaces) { this.currentFreePlaces = currentFreePlaces; }
    public void setPredictedFreePlaces(Integer predictedFreePlaces) { this.predictedFreePlaces = predictedFreePlaces; }
    public void setArrivalOption(String arrivalOption) { this.arrivalOption = arrivalOption; }
    public void setDistanceMeters(Integer distanceMeters) { this.distanceMeters = distanceMeters; }
    public void setDistanceText(String distanceText) { this.distanceText = distanceText; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
}
