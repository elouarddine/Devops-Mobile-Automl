package com.example.parksmart.models.admin;

import com.google.gson.annotations.SerializedName;

public class AdminDashboardData {
    @SerializedName("api_status")
    private String apiStatus;
    private AdminModelInfo model;
    private AdminDatasetInfo dataset;
    @SerializedName("last_training_summary")
    private String lastTrainingSummary;

    public String getApiStatus() { return apiStatus; }
    public AdminModelInfo getModel() { return model; }
    public AdminDatasetInfo getDataset() { return dataset; }
    public String getLastTrainingSummary() { return lastTrainingSummary; }
}
