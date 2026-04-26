package com.example.parksmart.models.admin;

import com.google.gson.annotations.SerializedName;

public class AdminDatasetInfo {
    private String name;
    @SerializedName("last_update")
    private String lastUpdate;

    public String getName() { return name; }
    public String getLastUpdate() { return lastUpdate; }
}
