package com.example.parksmart.repository.home;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.parksmart.models.home.ApiError;
import com.example.parksmart.models.home.AuthMeResponse;
import com.example.parksmart.models.home.BasicApiResponse;
import com.example.parksmart.models.home.HistoryApiResponse;
import com.example.parksmart.models.home.HistoryItem;
import com.example.parksmart.models.home.ParkingDetail;
import com.example.parksmart.models.home.ParkingDetailApiResponse;
import com.example.parksmart.models.home.ParkingPredictionRequest;
import com.example.parksmart.models.home.ParkingResult;
import com.example.parksmart.models.home.SaveParkingRequest;
import com.example.parksmart.models.home.SavedParkingItem;
import com.example.parksmart.models.home.SavedParkingsApiResponse;
import com.example.parksmart.models.home.SearchApiResponse;
import com.example.parksmart.models.home.SearchData;
import com.example.parksmart.models.home.SearchRequest;
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

public class HomeRepository {

    private final ApiService apiService;
    private final Gson gson = new Gson();

    public HomeRepository(Context context) {
        this(ApiClient.createAuthenticatedService(context));
    }

    public HomeRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public void searchParkings(SearchRequest request, SearchCallback callback) {
        apiService.searchParkings(request).enqueue(new Callback<SearchApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchApiResponse> call, @NonNull Response<SearchApiResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(extractErrorMessage(response, "Impossible de récupérer les parkings pour le moment."));
                    return;
                }

                SearchApiResponse apiResponse = response.body();
                SearchData data = apiResponse.getData();
                if (data == null) {
                    String message = apiResponse.getError() != null && apiResponse.getError().getMessage() != null
                            ? apiResponse.getError().getMessage()
                            : "Aucun résultat reçu depuis le serveur.";
                    callback.onError(message);
                    return;
                }

                List<ParkingResult> results = data.getResults() == null ? Collections.emptyList() : data.getResults();
                String recommendedParkingId = data.getRecommended() != null ? data.getRecommended().getParkingId() : null;
                Double destinationLat = data.getDestination() != null ? data.getDestination().getLatitude() : null;
                Double destinationLon = data.getDestination() != null ? data.getDestination().getLongitude() : null;
                callback.onSuccess(results, recommendedParkingId, destinationLat, destinationLon);
            }

            @Override
            public void onFailure(@NonNull Call<SearchApiResponse> call, @NonNull Throwable t) {
                callback.onError("Recherche indisponible. Vérifie que l'API tourne bien, que l'URL du backend est correcte, et que le téléphone/émulateur peut joindre le serveur.");
            }
        });
    }

    public void fetchHistory(HistoryCallback callback) {
        apiService.getHistory().enqueue(new Callback<HistoryApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<HistoryApiResponse> call, @NonNull Response<HistoryApiResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    callback.onError(extractErrorMessage(response, "Historique indisponible."));
                    return;
                }
                List<HistoryItem> items = response.body().getData().getItems();
                callback.onSuccess(items == null ? Collections.emptyList() : items);
            }

            @Override
            public void onFailure(@NonNull Call<HistoryApiResponse> call, @NonNull Throwable t) {
                callback.onError("Historique indisponible.");
            }
        });
    }

    public void deleteHistoryItem(int historyId, ActionCallback callback) {
        apiService.deleteHistoryItem(historyId).enqueue(callbackAdapter(callback, "Impossible de supprimer cet élément de l'historique."));
    }

    public void clearHistory(ActionCallback callback) {
        apiService.clearHistory().enqueue(callbackAdapter(callback, "Impossible de vider l'historique."));
    }

    public void fetchSavedParkings(SavedParkingsCallback callback) {
        apiService.getSavedParkings().enqueue(new Callback<SavedParkingsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<SavedParkingsApiResponse> call, @NonNull Response<SavedParkingsApiResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    callback.onError(extractErrorMessage(response, "Impossible de charger les sauvegardes."));
                    return;
                }
                List<SavedParkingItem> items = response.body().getData().getItems();
                callback.onSuccess(items == null ? Collections.emptyList() : items);
            }

            @Override
            public void onFailure(@NonNull Call<SavedParkingsApiResponse> call, @NonNull Throwable t) {
                callback.onError("Impossible de charger les sauvegardes.");
            }
        });
    }

    public void saveParking(SaveParkingRequest request, ActionCallback callback) {
        apiService.saveParking(request).enqueue(callbackAdapter(callback, "Impossible de sauvegarder ce parking."));
    }

    public void removeSavedParking(String parkingId, ActionCallback callback) {
        apiService.deleteSavedParking(parkingId).enqueue(callbackAdapter(callback, "Impossible de retirer ce parking."));
    }

    public void clearSavedParkings(ActionCallback callback) {
        apiService.clearSavedParkings().enqueue(callbackAdapter(callback, "Impossible de supprimer les parkings sauvegardés."));
    }

    public void fetchProfile(ProfileCallback callback) {
        apiService.getCurrentUser().enqueue(new Callback<AuthMeResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthMeResponse> call, @NonNull Response<AuthMeResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    callback.onError(extractErrorMessage(response, "Profil indisponible."));
                    return;
                }
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(@NonNull Call<AuthMeResponse> call, @NonNull Throwable t) {
                callback.onError("Profil indisponible.");
            }
        });
    }

    public void fetchParkingDetails(String parkingId, DetailCallback callback) {
        apiService.getParkingDetails(parkingId).enqueue(new Callback<ParkingDetailApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ParkingDetailApiResponse> call, @NonNull Response<ParkingDetailApiResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    callback.onError(extractErrorMessage(response, "Détails indisponibles."));
                    return;
                }
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(@NonNull Call<ParkingDetailApiResponse> call, @NonNull Throwable t) {
                callback.onError("Détails indisponibles.");
            }
        });
    }

    public void predictParking(ParkingPredictionRequest request, DetailCallback callback) {
        apiService.predictParking(request).enqueue(new Callback<ParkingDetailApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ParkingDetailApiResponse> call, @NonNull Response<ParkingDetailApiResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    callback.onError(extractErrorMessage(response, "Prédiction disponible."));
                    return;
                }
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(@NonNull Call<ParkingDetailApiResponse> call, @NonNull Throwable t) {
                callback.onError("Prédiction disponible.");
            }
        });
    }

    private Callback<BasicApiResponse> callbackAdapter(ActionCallback callback, String fallbackMessage) {
        return new Callback<BasicApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicApiResponse> call, @NonNull Response<BasicApiResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(extractErrorMessage(response, fallbackMessage));
                }
            }

            @Override
            public void onFailure(@NonNull Call<BasicApiResponse> call, @NonNull Throwable t) {
                callback.onError(fallbackMessage);
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

    public interface SearchCallback {
        void onSuccess(List<ParkingResult> results, String recommendedParkingId, Double destinationLat, Double destinationLon);
        void onError(String message);
    }

    public interface HistoryCallback {
        void onSuccess(List<HistoryItem> items);
        void onError(String message);
    }

    public interface SavedParkingsCallback {
        void onSuccess(List<SavedParkingItem> items);
        void onError(String message);
    }

    public interface ProfileCallback {
        void onSuccess(UserProfile profile);
        void onError(String message);
    }

    public interface DetailCallback {
        void onSuccess(ParkingDetail detail);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
