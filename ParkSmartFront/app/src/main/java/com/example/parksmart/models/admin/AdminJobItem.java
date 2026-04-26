package com.example.parksmart.models.admin;

import com.google.gson.annotations.SerializedName;

public class AdminJobItem {
    @SerializedName("job_id")
    private String jobId;
    private String type;
    private String status;
    @SerializedName("started_at")
    private String startedAt;
    @SerializedName("duration_sec")
    private Integer durationSec;

    public String getJobId() { return jobId; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getStartedAt() { return startedAt; }
    public Integer getDurationSec() { return durationSec; }
}
