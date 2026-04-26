package com.example.parksmart.controllers;

import com.example.parksmart.models.home.HistoryItem;
import com.example.parksmart.models.home.ParkingDetail;
import com.example.parksmart.models.home.ParkingPredictionRequest;
import com.example.parksmart.models.home.ParkingResult;
import com.example.parksmart.models.home.ParkingUiModel;
import com.example.parksmart.models.home.SaveParkingRequest;
import com.example.parksmart.models.home.SavedParkingItem;
import com.example.parksmart.models.home.SearchRequest;
import com.example.parksmart.models.home.UserProfile;
import com.example.parksmart.repository.home.HomeRepository;
import com.example.parksmart.utils.SessionManager;
import com.example.parksmart.view.home.UserHomeView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UserHomeController {

    private final UserHomeView view;
    private final HomeRepository repository;
    private final SessionManager sessionManager;

    private List<ParkingUiModel> currentSearchResults = new ArrayList<>();
    private List<ParkingUiModel> currentSavedParkings = new ArrayList<>();
    private List<HistoryItem> currentHistory = new ArrayList<>();
    private final Set<String> savedParkingIds = new HashSet<>();

    private String currentDestination = "";
    private String currentArrivalOption = "plus_30";
    private String currentEmail = "";
    private String currentName = "";
    private String currentRole = "";

    public UserHomeController(UserHomeView view, HomeRepository repository, SessionManager sessionManager) {
        this.view = view;
        this.repository = repository;
        this.sessionManager = sessionManager;
        this.currentName = sessionManager.getUserName();
        this.currentRole = sessionManager.getRole();
    }

    public void loadInitialState() {
        refreshSavedSection();
        refreshHistory();
        refreshProfile();
    }

    public void search(String destinationText, Double destinationLat, Double destinationLon, String arrivalOption) {
        if (destinationText == null || destinationText.trim().isEmpty()) {
            view.showMessage("Saisis une adresse ou un lieu à rechercher.");
            return;
        }

        currentDestination = destinationText.trim();
        currentArrivalOption = arrivalOption == null ? "plus_30" : arrivalOption;

        repository.searchParkings(new SearchRequest(currentDestination, destinationLat, destinationLon, currentArrivalOption, 2000), new HomeRepository.SearchCallback() {
            @Override
            public void onSuccess(List<ParkingResult> results, String recommendedParkingId, Double resolvedDestinationLat, Double resolvedDestinationLon) {
                currentSearchResults = mapResults(results, recommendedParkingId, currentArrivalOption);
                view.showSearchResults(currentDestination, resolvedDestinationLat, resolvedDestinationLon, currentSearchResults, findRecommendedName(recommendedParkingId, currentSearchResults));
                refreshHistory();
            }

            @Override
            public void onError(String message) {
                view.showMessage(message);
            }
        });
    }

    public void refreshSavedSection() {
        repository.fetchSavedParkings(new HomeRepository.SavedParkingsCallback() {
            @Override
            public void onSuccess(List<SavedParkingItem> items) {
                currentSavedParkings = mapSavedItems(items);
                savedParkingIds.clear();
                for (ParkingUiModel item : currentSavedParkings) {
                    savedParkingIds.add(item.getParkingId());
                }
                syncSavedState(currentSearchResults);
                view.showSavedParkings(currentSavedParkings);
                updateProfileSummary();
            }

            @Override
            public void onError(String message) {
                view.showSavedParkings(currentSavedParkings);
                updateProfileSummary();
            }
        });
    }

    public void refreshHistory() {
        repository.fetchHistory(new HomeRepository.HistoryCallback() {
            @Override
            public void onSuccess(List<HistoryItem> items) {
                currentHistory = items == null ? new ArrayList<>() : items;
                view.showSearchHistory(currentHistory);
                updateProfileSummary();
            }

            @Override
            public void onError(String message) {
                view.showSearchHistory(currentHistory);
                updateProfileSummary();
            }
        });
    }

    public void deleteHistoryItem(HistoryItem item) {
        if (item == null) {
            return;
        }
        view.showLoading(true);
        repository.deleteHistoryItem(item.getId(), new HomeRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                view.showLoading(false);
                refreshHistory();
                view.showMessage("Élément supprimé de l’historique.");
            }

            @Override
            public void onError(String message) {
                view.showLoading(false);
                view.showMessage(message);
            }
        });
    }

    public void clearHistory() {
        view.showLoading(true);
        repository.clearHistory(new HomeRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                view.showLoading(false);
                currentHistory = new ArrayList<>();
                view.showSearchHistory(currentHistory);
                updateProfileSummary();
                view.showMessage("Historique vidé.");
            }

            @Override
            public void onError(String message) {
                view.showLoading(false);
                view.showMessage(message);
            }
        });
    }

    public void clearSavedParkings() {
        view.showLoading(true);
        repository.clearSavedParkings(new HomeRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                view.showLoading(false);
                currentSavedParkings = new ArrayList<>();
                savedParkingIds.clear();
                syncSavedState(currentSearchResults);
                view.showSavedParkings(currentSavedParkings);
                updateProfileSummary();
                view.showMessage("Parkings sauvegardés supprimés.");
            }

            @Override
            public void onError(String message) {
                view.showLoading(false);
                view.showMessage(message);
            }
        });
    }

    public void refreshProfile() {
        repository.fetchProfile(new HomeRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (profile != null) {
                    currentName = profile.getFullName();
                    currentRole = profile.getRole();
                    currentEmail = profile.getEmail();
                }
                updateProfileSummary();
            }

            @Override
            public void onError(String message) {
                updateProfileSummary();
            }
        });
    }

    public void onHistorySelected(HistoryItem item) {
        if (item == null) {
            return;
        }
        currentDestination = item.getDestinationText() == null ? "" : item.getDestinationText();
        currentArrivalOption = item.getArrivalOption() == null ? "plus_30" : item.getArrivalOption();
        currentSearchResults = mapResults(item.getResults(), resolveRecommendedId(item), currentArrivalOption);
        view.showSearchResults(currentDestination, item.getDestinationLat(), item.getDestinationLon(), currentSearchResults, item.getRecommendedParking());
    }

    public void toggleSaved(ParkingUiModel parking) {
        if (parking == null || parking.getParkingId() == null) {
            view.showMessage("Parking invalide.");
            return;
        }

        view.showLoading(true);
        if (parking.isSaved()) {
            repository.removeSavedParking(parking.getParkingId(), new HomeRepository.ActionCallback() {
                @Override
                public void onSuccess() {
                    view.showLoading(false);
                    savedParkingIds.remove(parking.getParkingId());
                    parking.setSaved(false);
                    refreshSavedSection();
                    view.showMessage("Parking retiré des sauvegardes.");
                }

                @Override
                public void onError(String message) {
                    view.showLoading(false);
                    view.showMessage(message);
                }
            });
        } else {
            repository.saveParking(new SaveParkingRequest(parking), new HomeRepository.ActionCallback() {
                @Override
                public void onSuccess() {
                    view.showLoading(false);
                    savedParkingIds.add(parking.getParkingId());
                    parking.setSaved(true);
                    refreshSavedSection();
                    view.showMessage("Parking ajouté aux sauvegardes.");
                }

                @Override
                public void onError(String message) {
                    view.showLoading(false);
                    view.showMessage(message);
                }
            });
        }
    }

    public void loadParkingDetails(ParkingUiModel item) {
        if (item == null || item.getParkingId() == null) {
            view.showMessage("Détails indisponibles.");
            return;
        }

        view.showLoading(true);
        repository.predictParking(new ParkingPredictionRequest(item), new HomeRepository.DetailCallback() {
            @Override
            public void onSuccess(ParkingDetail detail) {
                view.showLoading(false);
                hydrateParkingAfterPrediction(item, detail);
                view.showParkingDetails(item);
            }

            @Override
            public void onError(String message) {
                view.showLoading(false);
                view.showMessage(message);
                view.showParkingDetails(item);
            }
        });
    }

    public List<ParkingUiModel> getCurrentSearchResults() {
        return currentSearchResults;
    }

    public String getCurrentDestination() {
        return currentDestination;
    }

    public String getCurrentArrivalOption() {
        return currentArrivalOption;
    }

    private void updateProfileSummary() {
        view.showProfileSummary(
                safe(currentName, sessionManager.getUserName(), "Utilisateur"),
                safe(currentRole, sessionManager.getRole(), "Utilisateur"),
                currentEmail == null ? "" : currentEmail,
                currentSavedParkings.size(),
                currentHistory.size()
        );
    }

    private void hydrateParkingAfterPrediction(ParkingUiModel item, ParkingDetail detail) {
        if (detail == null || item == null) {
            return;
        }
        item.setAddress(detail.getAddress());
        item.setLatitude(detail.getLatitude());
        item.setLongitude(detail.getLongitude());
        item.setCapacity(detail.getCapacity());
        item.setPricePerHour(detail.getPricePerHour());
        item.setCurrentFreePlaces(detail.getCurrentFreePlaces());
        item.setPredictedFreePlaces(detail.getPredictedFreePlaces());
        item.setDistanceMeters(detail.getDistanceMeters() != null ? detail.getDistanceMeters() : item.getDistanceMeters());
        item.setDistanceText(formatDistance(item.getDistanceMeters()));
        item.setArrivalOption(detail.getArrivalOption() == null ? item.getArrivalOption() : detail.getArrivalOption());
        item.setSourceName(detail.getSourceName() == null ? item.getSourceName() : detail.getSourceName());
        item.setAvailabilityText(buildAvailabilityText(item.getCurrentFreePlaces(), item.getDistanceMeters()));
        item.setPredictionText(buildPredictionText(item.getPredictedFreePlaces(), item.isRecommended(), item.getArrivalOption()));
        item.setDetailsText(buildPredictedDetailsText(item.getArrivalOption(), item.getSourceName(), detail.getPredictionFallbackUsed(), detail.getPredictionReason()));
    }

    private List<ParkingUiModel> mapResults(List<ParkingResult> results, String recommendedParkingId, String arrivalOption) {
        List<ParkingUiModel> mapped = new ArrayList<>();
        if (results == null) {
            return mapped;
        }

        for (ParkingResult result : results) {
            String parkingId = result.getParkingId();
            Integer predictedPlaces = null;
            boolean recommended = parkingId != null && parkingId.equals(recommendedParkingId);
            boolean saved = parkingId != null && savedParkingIds.contains(parkingId);

            mapped.add(new ParkingUiModel(
                    parkingId,
                    result.getName() == null ? "Parking" : result.getName(),
                    result.getAddress(),
                    result.getLatitude(),
                    result.getLongitude(),
                    result.getDistanceMeters(),
                    formatDistance(result.getDistanceMeters()),
                    buildAvailabilityText(result.getCurrentFreePlaces(), result.getDistanceMeters()),
                    buildPredictionText(predictedPlaces, recommended, arrivalOption),
                    buildPendingDetailsText(arrivalOption, result.getSourceName()),
                    result.getCurrentFreePlaces(),
                    predictedPlaces,
                    result.getCapacity(),
                    result.getPricePerHour(),
                    arrivalOption,
                    result.getSourceName(),
                    saved,
                    recommended
            ));
        }

        return mapped;
    }

    private List<ParkingUiModel> mapSavedItems(List<SavedParkingItem> items) {
        List<ParkingUiModel> mapped = new ArrayList<>();
        if (items == null) {
            return mapped;
        }
        for (SavedParkingItem item : items) {
            Integer predictedPlaces = item.getPredictedFreePlaces();
            mapped.add(new ParkingUiModel(
                    item.getParkingId(),
                    item.getName() == null ? "Parking" : item.getName(),
                    item.getAddress(),
                    item.getLatitude(),
                    item.getLongitude(),
                    item.getDistanceMeters(),
                    formatDistance(item.getDistanceMeters()),
                    buildAvailabilityText(item.getCurrentFreePlaces(), item.getDistanceMeters()),
                    buildPredictionText(predictedPlaces, false, item.getArrivalOption()),
                    buildPredictedDetailsText(item.getArrivalOption(), "sauvegarde", false, null),
                    item.getCurrentFreePlaces(),
                    predictedPlaces,
                    item.getCapacity(),
                    item.getPricePerHour(),
                    item.getArrivalOption(),
                    "sauvegarde",
                    true,
                    false
            ));
        }
        return mapped;
    }

    private void syncSavedState(List<ParkingUiModel> items) {
        if (items == null) return;
        for (ParkingUiModel item : items) {
            item.setSaved(savedParkingIds.contains(item.getParkingId()));
        }
    }

    private String findRecommendedName(String recommendedParkingId, List<ParkingUiModel> items) {
        if (recommendedParkingId == null || items == null) return "";
        for (ParkingUiModel item : items) {
            if (recommendedParkingId.equals(item.getParkingId())) return item.getName();
        }
        return "";
    }

    private String resolveRecommendedId(HistoryItem item) {
        if (item == null || item.getResults() == null || item.getRecommendedParking() == null) {
            return null;
        }
        for (ParkingResult result : item.getResults()) {
            if (item.getRecommendedParking().equals(result.getName())) {
                return result.getParkingId();
            }
        }
        return null;
    }

    private String buildPredictionText(Integer predictedPlaces, boolean recommended, String arrivalOption) {
        String label;
        if (predictedPlaces == null) {
            label = "Touchez pour lancer la prédiction";
        } else if (predictedPlaces > 0) {
            label = "Oui • place probable d’ici " + labelArrivalOption(arrivalOption);
        } else {
            label = "Non • aucune place probable d’ici " + labelArrivalOption(arrivalOption);
        }
        if (recommended && predictedPlaces != null) {
            return "Recommandé • " + label;
        }
        return label;
    }

    private String buildAvailabilityText(Integer currentFreePlaces, Integer distanceMeters) {
        if (currentFreePlaces != null) {
            if (distanceMeters == null) {
                return currentFreePlaces + " place(s) actuellement visibles";
            }
            return currentFreePlaces + " place(s) visibles • à " + formatDistance(distanceMeters);
        }
        if (distanceMeters == null) {
            return "Parking repéré autour de votre destination";
        }
        return "À environ " + formatDistance(distanceMeters) + " de la destination";
    }

    private String buildPendingDetailsText(String arrivalOption, String sourceName) {
        String sourceText = (sourceName == null || sourceName.trim().isEmpty()) ? "source inconnue" : sourceName.trim();
        return "Choisissez ce parking pour lancer la prédiction • arrivée " + labelArrivalOption(arrivalOption) + " • " + sourceText;
    }

    private String buildPredictedDetailsText(String arrivalOption, String sourceName, Boolean fallbackUsed, String predictionReason) {
        String sourceText = (sourceName == null || sourceName.trim().isEmpty()) ? "source inconnue" : sourceName.trim();
        String suffix = Boolean.TRUE.equals(fallbackUsed) ? " • backup utilisé" : "";
        if (predictionReason != null && Boolean.TRUE.equals(fallbackUsed)) {
            suffix += " (" + predictionReason + ")";
        }
        return "Itinéraire vers le parking • arrivée " + labelArrivalOption(arrivalOption) + " • " + sourceText + suffix;
    }

    private String labelArrivalOption(String arrivalOption) {
        if ("plus_15".equals(arrivalOption) || "now".equals(arrivalOption)) return "+15 min";
        if ("plus_60".equals(arrivalOption)) return "+60 min";
        if ("plus_30".equals(arrivalOption)) return "+30 min";
        return "durée non choisie";
    }

    private String formatDistance(Integer distanceMeters) {
        if (distanceMeters == null) return "Distance inconnue";
        if (distanceMeters >= 1000) return String.format(Locale.getDefault(), "%.1f km", distanceMeters / 1000f);
        return distanceMeters + " m";
    }

    private String safe(String value, String fallback, String defaultValue) {
        if (value != null && !value.trim().isEmpty()) return value.trim();
        if (fallback != null && !fallback.trim().isEmpty()) return fallback.trim();
        return defaultValue;
    }
}
