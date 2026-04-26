package com.example.parksmart.models.admin;

import com.google.gson.annotations.SerializedName;

public class AdminDatasetImportRequest {
    @SerializedName("dataset_name")
    private final String datasetName;
    @SerializedName("source_type")
    private final String sourceType;
    @SerializedName("source_url")
    private final String sourceUrl;

    public AdminDatasetImportRequest(String datasetName, String sourceUrl) {
        this.datasetName = datasetName;
        this.sourceType = "url";
        this.sourceUrl = sourceUrl;
    }
}
