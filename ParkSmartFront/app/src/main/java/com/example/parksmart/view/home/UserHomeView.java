package com.example.parksmart.view.home;

import com.example.parksmart.models.home.HistoryItem;
import com.example.parksmart.models.home.ParkingUiModel;

import java.util.List;

public interface UserHomeView {
    void showLoading(boolean isLoading);
    void showSearchResults(String destination, Double destinationLat, Double destinationLon, List<ParkingUiModel> results, String recommendedName);
    void showSavedParkings(List<ParkingUiModel> savedParkings);
    void showSearchHistory(List<HistoryItem> history);
    void showProfileSummary(String fullName, String role, String email, int savedCount, int historyCount);
    void showParkingDetails(ParkingUiModel item);
    void showMessage(String message);
}
