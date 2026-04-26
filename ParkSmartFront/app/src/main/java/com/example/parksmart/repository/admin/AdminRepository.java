package com.example.parksmart.repository.admin;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.parksmart.models.admin.AdminDashboardData;
import com.example.parksmart.models.admin.AdminDashboardResponse;
import com.example.parksmart.models.admin.AdminDatasetImportRequest;
import com.example.parksmart.models.admin.AdminJobItem;
import com.example.parksmart.models.admin.AdminJobsResponse;
import com.example.parksmart.models.admin.AdminResultsData;
import com.example.parksmart.models.admin.AdminResultsResponse;
import com.example.parksmart.models.home.ApiError;
import com.example.parksmart.models.home.AuthMeResponse;
import com.example.parksmart.models.home.BasicApiResponse;
import com.example.parksmart.models.home.UserProfile;
import com.example.parksmart.network.ApiClient;
import com.example.parksmart.network.ApiService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {

    private final ApiService apiService;
    private final Gson gson = new Gson();

    public AdminRepository(Context context) {
        this(ApiClient.createAuthenticatedService(context));
    }

    public AdminRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void fetchDashboard(DashboardCallback callback) {
        apiService.getAdminDashboard().enqueue(new Callback<AdminDashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminDashboardResponse> call, @NonNull Response<AdminDashboardResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    callback.onError(extractErrorMessage(response, "Impossible de charger le dashboard admin."));
                    return;
                }
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(@NonNull Call<AdminDashboardResponse> call, @NonNull Throwable t) {
                callback.onError("Impossible de charger le dashboard admin.");
            }
        });
    }

    public void importDataset(String datasetName, String sourceUrl, ActionCallback callback) {
        apiService.importAdminDataset(new AdminDatasetImportRequest(datasetName, sourceUrl)).enqueue(actionAdapter(callback, "Import du dataset impossible."));
    }

    public void launchTrain(ActionCallback callback) {
        apiService.launchAdminTrain().enqueue(actionAdapter(callback, "Entraînement indisponible pour le moment."));
    }

    public void launchEvaluate(ActionCallback callback) {
        apiService.launchAdminEvaluate().enqueue(actionAdapter(callback, "Évaluation indisponible pour le moment."));
    }

    public void fetchResults(String mode, ResultsCallback callback) {
        apiService.getAdminResults(mode).enqueue(new Callback<AdminResultsResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminResultsResponse> call, @NonNull Response<AdminResultsResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    callback.onError(extractErrorMessage(response, "Aucun résultat disponible pour le moment."));
                    return;
                }
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(@NonNull Call<AdminResultsResponse> call, @NonNull Throwable t) {
                callback.onError("Aucun résultat disponible pour le moment.");
            }
        });
    }

    public void fetchJobs(JobsCallback callback) {
        apiService.getAdminJobs().enqueue(new Callback<AdminJobsResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminJobsResponse> call, @NonNull Response<AdminJobsResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    callback.onError(extractErrorMessage(response, "Impossible de charger l’historique des jobs."));
                    return;
                }
                List<AdminJobItem> jobs = response.body().getData().getJobs();
                callback.onSuccess(jobs == null ? Collections.emptyList() : jobs);
            }

            @Override
            public void onFailure(@NonNull Call<AdminJobsResponse> call, @NonNull Throwable t) {
                callback.onError("Impossible de charger l’historique des jobs.");
            }
        });
    }

    public void fetchProfile(ProfileCallback callback) {
        apiService.getCurrentUser().enqueue(new Callback<AuthMeResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthMeResponse> call, @NonNull Response<AuthMeResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    callback.onError(extractErrorMessage(response, "Profil admin indisponible."));
                    return;
                }
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(@NonNull Call<AuthMeResponse> call, @NonNull Throwable t) {
                callback.onError("Profil admin indisponible.");
            }
        });
    }

    private Callback<BasicApiResponse> actionAdapter(ActionCallback callback, String fallback) {
        return new Callback<BasicApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicApiResponse> call, @NonNull Response<BasicApiResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(extractErrorMessage(response, fallback));
                }
            }

            @Override
            public void onFailure(@NonNull Call<BasicApiResponse> call, @NonNull Throwable t) {
                callback.onError(fallback);
            }
        };
    }

    private String extractErrorMessage(Response<?> response, String fallback) {
        if (response == null) {
            return fallback;
        }
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                if (raw != null && !raw.isEmpty()) {
                    BasicApiResponse parsed = gson.fromJson(raw, BasicApiResponse.class);
                    ApiError error = parsed != null ? parsed.getError() : null;
                    if (error != null && error.getMessage() != null && !error.getMessage().trim().isEmpty()) {
                        return error.getMessage();
                    }
                }
            }
        } catch (IOException ignored) {
        } catch (Exception ignored) {
        }
        return fallback;
    }

    public interface DashboardCallback {
        void onSuccess(AdminDashboardData data);
        void onError(String message);
    }

    public interface ResultsCallback {
        void onSuccess(AdminResultsData data);
        void onError(String message);
    }

    public interface JobsCallback {
        void onSuccess(List<AdminJobItem> jobs);
        void onError(String message);
    }

    public interface ProfileCallback {
        void onSuccess(UserProfile profile);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
