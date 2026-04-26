package com.example.parksmart.models.home;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("full_name")
    private String fullName;
    private String email;
    private String role;

    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
