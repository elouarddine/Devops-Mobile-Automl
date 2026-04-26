package com.example.parksmart.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.parksmart.models.home.ParkingUiModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HomeLocalStore {

    private static final String PREF_NAME = "park_smart_home";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final String KEY_SAVED_PARKINGS = "saved_parkings";
    private static final int MAX_HISTORY_SIZE = 12;

    private final SharedPreferences preferences;
    private final Gson gson;

    public HomeLocalStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<String> getSearchHistory() {
        Type type = new TypeToken<List<String>>() {}.getType();
        String json = preferences.getString(KEY_SEARCH_HISTORY, "[]");
        List<String> result = gson.fromJson(json, type);
        return result == null ? new ArrayList<>() : result;
    }

    public void saveSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        String cleanQuery = query.trim();
        List<String> history = getSearchHistory();
        history.remove(cleanQuery);
        history.add(0, cleanQuery);
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }
        preferences.edit().putString(KEY_SEARCH_HISTORY, gson.toJson(history)).apply();
    }

    public void removeSearchQuery(String query) {
        List<String> history = getSearchHistory();
        history.remove(query);
        preferences.edit().putString(KEY_SEARCH_HISTORY, gson.toJson(history)).apply();
    }

    public void clearSearchHistory() {
        preferences.edit().putString(KEY_SEARCH_HISTORY, "[]").apply();
    }

    public List<ParkingUiModel> getSavedParkings() {
        Type type = new TypeToken<List<ParkingUiModel>>() {}.getType();
        String json = preferences.getString(KEY_SAVED_PARKINGS, "[]");
        List<ParkingUiModel> result = gson.fromJson(json, type);
        return result == null ? new ArrayList<>() : result;
    }

    public boolean isParkingSaved(String parkingId) {
        for (ParkingUiModel item : getSavedParkings()) {
            if (parkingId != null && parkingId.equals(item.getParkingId())) {
                return true;
            }
        }
        return false;
    }

    public boolean toggleSavedParking(ParkingUiModel parking) {
        List<ParkingUiModel> saved = getSavedParkings();
        Iterator<ParkingUiModel> iterator = saved.iterator();
        while (iterator.hasNext()) {
            ParkingUiModel item = iterator.next();
            if (parking.getParkingId() != null && parking.getParkingId().equals(item.getParkingId())) {
                iterator.remove();
                preferences.edit().putString(KEY_SAVED_PARKINGS, gson.toJson(saved)).apply();
                return false;
            }
        }
        parking.setSaved(true);
        saved.add(0, parking);
        preferences.edit().putString(KEY_SAVED_PARKINGS, gson.toJson(saved)).apply();
        return true;
    }
}
