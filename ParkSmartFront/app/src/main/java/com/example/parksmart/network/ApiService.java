package com.example.parksmart.network;

import com.example.parksmart.models.auth.LoginRequest;
import com.example.parksmart.models.auth.LoginResponse;
import com.example.parksmart.models.auth.RegisterRequest;
import com.example.parksmart.models.auth.RegisterResponse;
import com.example.parksmart.models.admin.AdminDashboardResponse;
import com.example.parksmart.models.admin.AdminDatasetImportRequest;
import com.example.parksmart.models.admin.AdminJobsResponse;
import com.example.parksmart.models.admin.AdminResultsResponse;
import com.example.parksmart.models.home.AuthMeResponse;
import com.example.parksmart.models.home.BasicApiResponse;
import com.example.parksmart.models.home.HistoryApiResponse;
import com.example.parksmart.models.home.ParkingDetailApiResponse;
import com.example.parksmart.models.home.ParkingPredictionRequest;
import com.example.parksmart.models.home.SaveParkingRequest;
import com.example.parksmart.models.home.SavedParkingsApiResponse;
import com.example.parksmart.models.home.SearchApiResponse;
import com.example.parksmart.models.home.SearchRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @GET("auth/me")
    Call<AuthMeResponse> getCurrentUser();

    @POST("v1/search")
    Call<SearchApiResponse> searchParkings(@Body SearchRequest request);

    @POST("v1/parkings/predict")
    Call<ParkingDetailApiResponse> predictParking(@Body ParkingPredictionRequest request);

    @GET("v1/history")
    Call<HistoryApiResponse> getHistory();

    @DELETE("v1/history/{historyId}")
    Call<BasicApiResponse> deleteHistoryItem(@Path("historyId") int historyId);

    @DELETE("v1/history")
    Call<BasicApiResponse> clearHistory();

    @GET("v1/saved-parkings")
    Call<SavedParkingsApiResponse> getSavedParkings();

    @POST("v1/saved-parkings")
    Call<BasicApiResponse> saveParking(@Body SaveParkingRequest request);

    @DELETE("v1/saved-parkings/{parkingId}")
    Call<BasicApiResponse> deleteSavedParking(@Path("parkingId") String parkingId);

    @DELETE("v1/saved-parkings")
    Call<BasicApiResponse> clearSavedParkings();

    @GET("v1/parkings/{parkingId}")
    Call<ParkingDetailApiResponse> getParkingDetails(@Path("parkingId") String parkingId);

    @GET("v1/admin/dashboard")
    Call<AdminDashboardResponse> getAdminDashboard();

    @POST("v1/admin/datasets/import")
    Call<BasicApiResponse> importAdminDataset(@Body AdminDatasetImportRequest request);

    @POST("v1/admin/train")
    Call<BasicApiResponse> launchAdminTrain();

    @POST("v1/admin/evaluate")
    Call<BasicApiResponse> launchAdminEvaluate();

    @GET("v1/admin/results")
    Call<AdminResultsResponse> getAdminResults(@Query("mode") String mode);

    @GET("v1/admin/jobs")
    Call<AdminJobsResponse> getAdminJobs();
}
