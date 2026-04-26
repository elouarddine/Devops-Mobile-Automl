package com.example.parksmart.fragments.user;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.parksmart.R;
import com.example.parksmart.adapters.ParkingCardAdapter;
import com.example.parksmart.adapters.SearchHistoryAdapter;
import com.example.parksmart.controllers.UserHomeController;
import com.example.parksmart.databinding.ActivityHomescreenBinding;
import com.example.parksmart.models.home.HistoryItem;
import com.example.parksmart.models.home.ParkingUiModel;
import com.example.parksmart.repository.home.HomeRepository;
import com.example.parksmart.utils.SessionManager;
import com.example.parksmart.view.home.UserHomeView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserHomeFragment extends Fragment implements UserHomeView,
        ParkingCardAdapter.Listener,
        SearchHistoryAdapter.Listener {

    private static final LatLng DEFAULT_CITY = new LatLng(48.0061, 0.1996);
    private static final float DEFAULT_ZOOM = 13.5f;
    private static final float DETAIL_ZOOM = 16f;
    private static final double SEARCH_RADIUS_METERS = 2000d;
    private static final int LOCATION_PERMISSION_REQUEST = 1008;

    private ActivityHomescreenBinding binding;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private ParkingCardAdapter parkingAdapter;
    private SearchHistoryAdapter historyAdapter;
    private UserHomeController controller;
    private SessionManager sessionManager;
    private MapView mapView;
    private GoogleMap googleMap;
    private boolean ignoreBottomNavCallback = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final OkHttpClient httpClient = new OkHttpClient();

    private final List<ParkingUiModel> savedItems = new ArrayList<>();
    private final List<ParkingUiModel> resultItems = new ArrayList<>();
    private LatLng currentSearchCenter;
    private LatLng currentRouteStart;
    private List<LatLng> currentRoutePoints;
    private Runnable pendingLocationAction;
    private String lastSearchLabel = "";

    private enum Section {
        SAVED,
        RESULTS,
        PROFILE,
        HISTORY,
        DETAIL
    }

    private Section currentSection = Section.SAVED;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ActivityHomescreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        controller = new UserHomeController(this, new HomeRepository(requireContext()), sessionManager);

        setupRecyclerViews();
        setupBottomSheet();
        setupClicks();
        setupBottomNavigation();
        setupSearchField();
        setupFilters();
        binding.searchOverlay.chipGroupArrival.clearCheck();
        setupMap(savedInstanceState);
        fillDefaultMapState();

        selectBottomNavItemSilently(R.id.nav_saved);
        controller.loadInitialState();
        showSavedSection();
    }

    private void setupRecyclerViews() {
        parkingAdapter = new ParkingCardAdapter(this);
        binding.sheetContent.rvSavedParkings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.sheetContent.rvSavedParkings.setAdapter(parkingAdapter);

        historyAdapter = new SearchHistoryAdapter(this);
        binding.searchOverlay.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.searchOverlay.rvHistory.setAdapter(historyAdapter);
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setDraggable(true);
        bottomSheetBehavior.setSkipCollapsed(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        syncSearchBars(true);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                syncSearchBars(newState == BottomSheetBehavior.STATE_COLLAPSED);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                boolean showTop = slideOffset < 0.18f;
                binding.btnSearchTop.setAlpha(showTop ? 1f : 0f);
                binding.btnSearchTop.setVisibility(showTop ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupClicks() {
        View.OnClickListener openSearchListener = v -> openSearchOverlay();
        binding.btnSearchTop.setOnClickListener(openSearchListener);
        binding.sheetContent.btnSearchInSheet.setOnClickListener(openSearchListener);
        binding.searchOverlay.btnBack.setOnClickListener(v -> closeSearchOverlay());
        binding.searchOverlay.btnSearchAction.setOnClickListener(v -> submitSearch());
        binding.searchOverlay.btnClearHistory.setOnClickListener(v -> confirmClearHistory());
        binding.sheetContent.btnClearSaved.setOnClickListener(v -> confirmClearSaved());
        binding.sheetContent.handle.setOnClickListener(v -> toggleBottomSheet());
        binding.btnRetryMap.setOnClickListener(v -> {
            binding.mapStatusCard.setVisibility(View.GONE);
            centerMap(DEFAULT_CITY, DEFAULT_ZOOM);
        });

        binding.profileOverlay.cardLogout.setOnClickListener(v -> logout());
        binding.profileOverlay.cardSavedParkings.setOnClickListener(v -> {
            selectBottomNavItemSilently(R.id.nav_saved);
            showSavedSection();
        });
        binding.profileOverlay.cardSearchHistory.setOnClickListener(v -> {
            selectBottomNavItemSilently(R.id.nav_history);
            openHistorySection();
        });
        binding.profileOverlay.cardAbout.setOnClickListener(v -> showMessage("ParkSmart • recherche, prédiction, itinéraires et sauvegardes connectées au backend"));
        binding.profileOverlay.cardPersonalInfo.setOnClickListener(v -> showMessage("L’édition du profil pourra être ajoutée ensuite."));

        binding.detailOverlay.btnCloseDetail.setOnClickListener(v -> closeDetailOverlay());
        binding.detailOverlay.btnDetailSave.setOnClickListener(v -> {
            ParkingUiModel item = (ParkingUiModel) binding.detailOverlay.btnDetailSave.getTag();
            if (item != null) {
                controller.toggleSaved(item);
            }
        });
        binding.detailOverlay.btnDetailRoute.setOnClickListener(v -> {
            ParkingUiModel item = (ParkingUiModel) binding.detailOverlay.btnDetailRoute.getTag();
            if (item != null) {
                openRouteToParking(item);
            }
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (ignoreBottomNavCallback) {
                return true;
            }
            int itemId = item.getItemId();
            if (itemId == R.id.nav_saved) {
                showSavedSection();
                return true;
            }
            if (itemId == R.id.nav_history) {
                openHistorySection();
                return true;
            }
            if (itemId == R.id.nav_profile) {
                openProfileSection();
                return true;
            }
            return false;
        });
    }

    private void setupSearchField() {
        binding.searchOverlay.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            boolean shouldSearch = actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN);
            if (shouldSearch) {
                submitSearch();
                return true;
            }
            return false;
        });
    }

    private void setupFilters() {
        binding.sheetContent.chipGroupFilters.setOnCheckedStateChangeListener((ChipGroup group, List<Integer> checkedIds) -> {
            int checkedId = group.getCheckedChipId();
            if (checkedId == R.id.chipNearby) {
                showArrivalChoiceDialog(this::startNearbySearch);
                return;
            }
            applyFilterAndRender();
        });
    }

    private void setupMap(@Nullable Bundle savedInstanceState) {
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        MapsInitializer.initialize(requireContext());
        mapView.getMapAsync(map -> {
            googleMap = map;
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setRotateGesturesEnabled(true);
            googleMap.getUiSettings().setTiltGesturesEnabled(true);
            googleMap.setOnMapClickListener(latLng -> {
                if (bottomSheetBehavior != null && binding.bottomSheet.getVisibility() == View.VISIBLE) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });
            googleMap.setOnMarkerClickListener(marker -> {
                Object tag = marker.getTag();
                if (tag instanceof ParkingUiModel) {
                    controller.loadParkingDetails((ParkingUiModel) tag);
                    return true;
                }
                return false;
            });
            try {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
            } catch (Exception ignored) {
            }
            centerMap(DEFAULT_CITY, DEFAULT_ZOOM);
            refreshMap();
        });
    }

    private void toggleBottomSheet() {
        if (bottomSheetBehavior == null) {
            return;
        }
        int state = bottomSheetBehavior.getState();
        if (state == BottomSheetBehavior.STATE_COLLAPSED) {
            expandBottomSheet();
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void expandBottomSheet() {
        if (bottomSheetBehavior == null || binding == null) {
            return;
        }
        binding.bottomSheet.post(() -> {
            if (binding == null || bottomSheetBehavior == null) {
                return;
            }
            bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.home_sheet_peek_collapsed));
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
    }

    private void submitSearch() {
        String query = binding.searchOverlay.etSearch.getText() == null ? "" : binding.searchOverlay.etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            showMessage("Saisis une adresse ou un lieu à rechercher.");
            return;
        }
        String arrivalOption = getSelectedArrivalOption();
        if (arrivalOption == null) {
            showMessage("Choisissez d’abord +15 min, +30 min ou +60 min.");
            return;
        }
        lastSearchLabel = query;
        currentRoutePoints = null;
        currentRouteStart = null;
        resolveAddressToCoordinates(query, point -> {
            currentSearchCenter = point;
            if (point != null) {
                drawSearchBaseState();
            } else {
                binding.tvMapHintChip.setText("Recherche en cours");
                binding.tvMapFocusTitle.setText(query);
                binding.tvMapFocusSubtitle.setText("Résolution de l’adresse par le backend…");
                binding.tvMapFocusRecommendation.setText("Les parkings proches et la prédiction vont s’afficher dès que la réponse arrive.");
            }
            controller.search(query, point != null ? point.latitude : null, point != null ? point.longitude : null, arrivalOption);
        });
    }

    private void tryDisplayRouteToDestination(@NonNull LatLng destination, boolean silentIfNoLocation) {
        if (hasLocationPermission()) {
            fetchCurrentLocation(location -> {
                if (location == null) {
                    if (!silentIfNoLocation) {
                        showMessage("Adresse trouvée. Activez la localisation pour afficher l’itinéraire complet.");
                    }
                    drawSearchBaseState();
                    return;
                }
                LatLng start = new LatLng(location.getLatitude(), location.getLongitude());
                requestRoute(start, destination, routePoints -> {
                    currentRouteStart = start;
                    currentRoutePoints = routePoints;
                    refreshMap();
                }, () -> {
                    currentRouteStart = start;
                    currentRoutePoints = buildFallbackStraightLine(start, destination);
                    refreshMap();
                });
            });
        } else if (!silentIfNoLocation) {
            pendingLocationAction = () -> tryDisplayRouteToDestination(destination, true);
            requestLocationPermission();
            drawSearchBaseState();
        } else {
            drawSearchBaseState();
        }
    }

    private String getSelectedArrivalOption() {
        int id = binding.searchOverlay.chipGroupArrival.getCheckedChipId();
        if (id == R.id.chipArrivalNow) return "plus_15";
        if (id == R.id.chipArrival30) return "plus_30";
        if (id == R.id.chipArrival60) return "plus_60";
        return null;
    }

    private void setSelectedArrivalOption(String arrivalOption) {
        if (!isAdded()) {
            return;
        }
        if ("plus_15".equals(arrivalOption) || "now".equals(arrivalOption)) {
            binding.searchOverlay.chipGroupArrival.check(R.id.chipArrivalNow);
        } else if ("plus_60".equals(arrivalOption)) {
            binding.searchOverlay.chipGroupArrival.check(R.id.chipArrival60);
        } else {
            binding.searchOverlay.chipGroupArrival.check(R.id.chipArrival30);
        }
    }

    private void selectBottomNavItemSilently(int itemId) {
        if (binding == null || binding.bottomNav.getSelectedItemId() == itemId) {
            return;
        }
        ignoreBottomNavCallback = true;
        binding.bottomNav.setSelectedItemId(itemId);
        binding.bottomNav.post(() -> ignoreBottomNavCallback = false);
    }

    private void openSearchOverlay() {
        currentSection = currentSection == Section.RESULTS ? Section.RESULTS : Section.HISTORY;
        binding.profileOverlayContainer.setVisibility(View.GONE);
        binding.detailOverlayContainer.setVisibility(View.GONE);
        binding.searchOverlayContainer.setVisibility(View.VISIBLE);
        binding.bottomSheet.setVisibility(View.GONE);
        binding.btnSearchTop.setVisibility(View.GONE);
        binding.searchOverlay.etSearch.requestFocus();
    }

    private void closeSearchOverlay() {
        binding.searchOverlayContainer.setVisibility(View.GONE);
        if (currentSection == Section.RESULTS) {
            showResultsSection();
        } else {
            showSavedSection();
        }
    }

    private void openHistorySection() {
        currentSection = Section.HISTORY;
        binding.profileOverlayContainer.setVisibility(View.GONE);
        binding.detailOverlayContainer.setVisibility(View.GONE);
        binding.searchOverlayContainer.setVisibility(View.VISIBLE);
        binding.bottomSheet.setVisibility(View.GONE);
        binding.btnSearchTop.setVisibility(View.GONE);
        binding.searchOverlay.etSearch.setText("");
        binding.searchOverlay.etSearch.clearFocus();
        controller.refreshHistory();
    }

    private void openProfileSection() {
        currentSection = Section.PROFILE;
        binding.searchOverlayContainer.setVisibility(View.GONE);
        binding.detailOverlayContainer.setVisibility(View.GONE);
        binding.profileOverlayContainer.setVisibility(View.VISIBLE);
        binding.bottomSheet.setVisibility(View.GONE);
        binding.btnSearchTop.setVisibility(View.GONE);
        controller.refreshProfile();
    }

    private void closeDetailOverlay() {
        binding.detailOverlayContainer.setVisibility(View.GONE);
        if (!controller.getCurrentSearchResults().isEmpty()) {
            showResultsSection();
        } else {
            showSavedSection();
        }
    }

    private void showSavedSection() {
        currentSection = Section.SAVED;
        binding.searchOverlayContainer.setVisibility(View.GONE);
        binding.profileOverlayContainer.setVisibility(View.GONE);
        binding.detailOverlayContainer.setVisibility(View.GONE);
        binding.bottomSheet.setVisibility(View.VISIBLE);
        binding.sheetContent.tvSavedTitle.setText(R.string.home_saved_title);
        binding.sheetContent.tvSheetSubtitle.setText(R.string.home_saved_subtitle);
        binding.sheetContent.tvEmptySavedTitle.setText(R.string.home_empty_saved_title);
        binding.sheetContent.tvEmptySavedSubtitle.setText(R.string.home_empty_saved_subtitle);
        binding.sheetContent.btnClearSaved.setVisibility(savedItems.isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvMapHintChip.setText(R.string.home_map_chip_saved);
        binding.tvMapFocusTitle.setText(R.string.home_saved_map_title);
        binding.tvMapFocusSubtitle.setText(R.string.home_saved_map_subtitle);
        binding.tvMapFocusRecommendation.setText(R.string.home_saved_map_recommendation);
        binding.tvSearchTop.setText(R.string.home_search_placeholder);
        controller.refreshSavedSection();
        expandBottomSheet();
        syncSearchBars(false);
        refreshMap();
    }

    private void showResultsSection() {
        currentSection = Section.RESULTS;
        binding.searchOverlayContainer.setVisibility(View.GONE);
        binding.profileOverlayContainer.setVisibility(View.GONE);
        binding.detailOverlayContainer.setVisibility(View.GONE);
        binding.bottomSheet.setVisibility(View.VISIBLE);
        selectBottomNavItemSilently(R.id.nav_saved);
        binding.sheetContent.btnClearSaved.setVisibility(View.GONE);
        binding.sheetContent.tvSavedTitle.setText(R.string.home_results_title);
        binding.sheetContent.tvSheetSubtitle.setText(getString(R.string.home_results_subtitle, controller.getCurrentDestination()));
        binding.sheetContent.tvEmptySavedTitle.setText(R.string.home_empty_results_title);
        binding.sheetContent.tvEmptySavedSubtitle.setText(R.string.home_empty_results_subtitle);
        expandBottomSheet();
        syncSearchBars(false);
        applyFilterAndRender();
        refreshMap();
    }

    private void syncSearchBars(boolean showTopSearch) {
        if (binding.bottomSheet.getVisibility() != View.VISIBLE) {
            binding.btnSearchTop.setVisibility(View.GONE);
            return;
        }
        binding.btnSearchTop.setVisibility(showTopSearch ? View.VISIBLE : View.GONE);
        binding.sheetContent.btnSearchInSheet.setVisibility(showTopSearch ? View.GONE : View.VISIBLE);
    }

    private void fillDefaultMapState() {
        binding.tvMapHintChip.setText(R.string.home_map_chip_default);
        binding.tvMapFocusTitle.setText(R.string.home_map_title_default);
        binding.tvMapFocusSubtitle.setText(R.string.home_map_subtitle_default);
        binding.tvMapFocusRecommendation.setText(R.string.home_map_recommendation_default);
        binding.tvSearchTop.setText(R.string.home_search_placeholder);
    }

    private void centerMap(LatLng point, float zoom) {
        if (googleMap == null) {
            return;
        }
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(point)
                .zoom(zoom)
                .build()));
    }

    private void refreshMap() {
        if (googleMap == null) {
            return;
        }
        googleMap.clear();
        if (currentSection == Section.RESULTS || currentSection == Section.DETAIL) {
            renderSearchMap(resultItems);
            return;
        }
        renderSavedMap();
    }

    private void renderSavedMap() {
        if (savedItems.isEmpty()) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(DEFAULT_CITY)
                    .title("Le Mans")
                    .snippet("Zone de démonstration ParkSmart")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            googleMap.addMarker(markerOptions);
            centerMap(DEFAULT_CITY, DEFAULT_ZOOM);
            return;
        }
        List<LatLng> boundsPoints = new ArrayList<>();
        for (ParkingUiModel item : savedItems) {
            if (item.getLatitude() == null || item.getLongitude() == null) continue;
            LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
            boundsPoints.add(position);
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(item.getName())
                    .snippet(item.getAvailabilityText())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            if (marker != null) marker.setTag(item);
        }
        moveCameraToBounds(boundsPoints);
    }

    private void renderSearchMap(List<ParkingUiModel> items) {
        List<LatLng> boundsPoints = new ArrayList<>();

        if (currentSearchCenter != null) {
            Marker destinationMarker = googleMap.addMarker(new MarkerOptions()
                    .position(currentSearchCenter)
                    .title(lastSearchLabel == null || lastSearchLabel.isEmpty() ? "Destination" : lastSearchLabel)
                    .snippet("Destination recherchée")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            if (destinationMarker != null) destinationMarker.setTag(null);
            boundsPoints.add(currentSearchCenter);
            googleMap.addCircle(new CircleOptions()
                    .center(currentSearchCenter)
                    .radius(SEARCH_RADIUS_METERS)
                    .strokeColor(0xFF2563EB)
                    .fillColor(0x1F2563EB)
                    .strokeWidth(4f));
        }

        if (currentRoutePoints != null && !currentRoutePoints.isEmpty()) {
            googleMap.addPolyline(new PolylineOptions()
                    .addAll(currentRoutePoints)
                    .color(0xFF2563EB)
                    .width(10f));
            boundsPoints.addAll(currentRoutePoints);
            if (currentRouteStart != null) {
                boolean sameAsSearchCenter = currentSearchCenter != null
                        && Math.abs(currentSearchCenter.latitude - currentRouteStart.latitude) < 0.0001
                        && Math.abs(currentSearchCenter.longitude - currentRouteStart.longitude) < 0.0001;
                if (!sameAsSearchCenter) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(currentRouteStart)
                            .title("Départ")
                            .snippet("Point de départ")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }
                boundsPoints.add(currentRouteStart);
            }
        }

        for (ParkingUiModel item : items) {
            if (item.getLatitude() == null || item.getLongitude() == null) continue;
            LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
            boundsPoints.add(position);
            float hue = item.isRecommended() ? BitmapDescriptorFactory.HUE_ORANGE : BitmapDescriptorFactory.HUE_AZURE;
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(item.getName())
                    .snippet(item.getPredictionText())
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
            if (marker != null) marker.setTag(item);
        }

        if (boundsPoints.isEmpty()) {
            centerMap(DEFAULT_CITY, DEFAULT_ZOOM);
        } else {
            moveCameraToBounds(boundsPoints);
        }
    }

    private void moveCameraToBounds(List<LatLng> points) {
        if (googleMap == null || points == null || points.isEmpty()) {
            return;
        }
        if (points.size() == 1) {
            centerMap(points.get(0), DETAIL_ZOOM);
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        binding.mapView.post(() -> googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 140)));
    }

    private void applyFilterAndRender() {
        List<ParkingUiModel> base = currentSection == Section.RESULTS ? resultItems : savedItems;
        List<ParkingUiModel> display = new ArrayList<>(base);
        int checkedId = binding.sheetContent.chipGroupFilters.getCheckedChipId();
        if (checkedId == R.id.chipNearby || checkedId == R.id.chipNearest) {
            Collections.sort(display, Comparator.comparing(item -> item.getDistanceMeters() == null ? Integer.MAX_VALUE : item.getDistanceMeters()));
        } else if (checkedId == R.id.chipCheapest) {
            Collections.sort(display, Comparator.comparing(item -> item.getPricePerHour() == null ? Double.MAX_VALUE : item.getPricePerHour()));
        } else if (checkedId == R.id.chipAvailability) {
            display.sort((first, second) -> Integer.compare(second.getPredictedFreePlaces(), first.getPredictedFreePlaces()));
        }
        parkingAdapter.submitList(display);
        boolean empty = display.isEmpty();
        binding.sheetContent.emptySaved.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.sheetContent.rvSavedParkings.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (currentSection == Section.RESULTS || currentSection == Section.DETAIL) {
            refreshMap();
        }
    }

    private void logout() {
        sessionManager.logout();
        Navigation.findNavController(requireView()).navigate(
                R.id.loginFragment,
                null,
                new NavOptions.Builder().setPopUpTo(R.id.userHomeFragment, true).build()
        );
    }

    private void openRouteToParking(ParkingUiModel item) {
        if (item.getLatitude() == null || item.getLongitude() == null) {
            showMessage("Coordonnées indisponibles pour cet itinéraire.");
            return;
        }
        LatLng destination = new LatLng(item.getLatitude(), item.getLongitude());
        if (currentSearchCenter != null) {
            LatLng start = currentSearchCenter;
            requestRoute(start, destination, routePoints -> {
                currentRouteStart = start;
                currentRoutePoints = routePoints;
                closeDetailOverlay();
                renderRouteText(item);
                refreshMap();
            }, () -> {
                currentRouteStart = start;
                currentRoutePoints = buildFallbackStraightLine(start, destination);
                closeDetailOverlay();
                renderRouteText(item);
                refreshMap();
            });
        } else if (hasLocationPermission()) {
            fetchCurrentLocation(location -> {
                LatLng start = location != null
                        ? new LatLng(location.getLatitude(), location.getLongitude())
                        : DEFAULT_CITY;
                requestRoute(start, destination, routePoints -> {
                    currentRouteStart = start;
                    currentRoutePoints = routePoints;
                    closeDetailOverlay();
                    renderRouteText(item);
                    refreshMap();
                }, () -> {
                    currentRouteStart = start;
                    currentRoutePoints = buildFallbackStraightLine(start, destination);
                    closeDetailOverlay();
                    renderRouteText(item);
                    refreshMap();
                });
            });
        } else {
            pendingLocationAction = () -> openRouteToParking(item);
            requestLocationPermission();
        }
    }

    private void renderRouteText(ParkingUiModel item) {
        binding.tvMapHintChip.setText("Itinéraire");
        binding.tvMapFocusTitle.setText(item.getName());
        binding.tvMapFocusSubtitle.setText("Itinéraire affiché depuis votre adresse recherchée vers ce parking");
        binding.tvMapFocusRecommendation.setText(item.getAddress() == null || item.getAddress().isEmpty() ? "Le trajet vers ce parking est prêt." : item.getAddress());
        if (currentSection != Section.DETAIL) {
            currentSection = Section.RESULTS;
        }
    }

    private void requestRoute(LatLng start, LatLng end, RouteSuccessListener successListener, Runnable failureListener) {
        try {
            String url = String.format(Locale.US,
                    "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                    start.longitude, start.latitude, end.longitude, end.latitude);
            Request request = new Request.Builder().url(url).build();
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    handler.post(failureListener);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        handler.post(failureListener);
                        return;
                    }
                    try {
                        JSONObject root = new JSONObject(response.body().string());
                        JSONArray routes = root.optJSONArray("routes");
                        if (routes == null || routes.length() == 0) {
                            handler.post(failureListener);
                            return;
                        }
                        JSONArray coordinates = routes.getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONArray("coordinates");
                        List<LatLng> points = new ArrayList<>();
                        for (int i = 0; i < coordinates.length(); i++) {
                            JSONArray point = coordinates.getJSONArray(i);
                            points.add(new LatLng(point.getDouble(1), point.getDouble(0)));
                        }
                        handler.post(() -> successListener.onRoute(points));
                    } catch (Exception e) {
                        handler.post(failureListener);
                    }
                }
            });
        } catch (Exception exception) {
            failureListener.run();
        }
    }

    private List<LatLng> buildFallbackStraightLine(LatLng start, LatLng end) {
        List<LatLng> fallback = new ArrayList<>();
        fallback.add(start);
        fallback.add(end);
        return fallback;
    }

    @Override
    public void showLoading(boolean isLoading) {
        binding.sheetContent.progressSheet.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.searchOverlay.progressSearch.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showSearchResults(String destination, Double destinationLat, Double destinationLon, List<ParkingUiModel> results, String recommendedName) {
        resultItems.clear();
        if (results != null) {
            resultItems.addAll(results);
        }

        if (destinationLat != null && destinationLon != null) {
            currentSearchCenter = new LatLng(destinationLat, destinationLon);
            currentRoutePoints = null;
            currentRouteStart = null;
        }

        binding.tvSearchTop.setText(destination);
        binding.tvMapHintChip.setText(getString(R.string.home_map_chip_results, resultItems.size()));
        binding.tvMapFocusTitle.setText(destination);
        if (resultItems.isEmpty()) {
            binding.tvMapFocusSubtitle.setText("Adresse trouvée, mais aucun parking n’a été renvoyé dans cette zone.");
            binding.tvMapFocusRecommendation.setText("La carte reste centrée sur votre destination. Vous pouvez tester une autre adresse ou élargir la base de données parking.");
        } else {
            binding.tvMapFocusSubtitle.setText(getString(R.string.home_map_subtitle_results, resultItems.size()));
            binding.tvMapFocusRecommendation.setText(recommendedName == null || recommendedName.isEmpty()
                    ? getString(R.string.home_map_recommendation_fallback)
                    : getString(R.string.home_map_recommendation, recommendedName));
        }

        showResultsSection();
        refreshMap();
    }

    @Override
    public void showSavedParkings(List<ParkingUiModel> savedParkings) {
        savedItems.clear();
        if (savedParkings != null) {
            savedItems.addAll(savedParkings);
        }
        binding.sheetContent.btnClearSaved.setVisibility(savedItems.isEmpty() || currentSection != Section.SAVED ? View.GONE : View.VISIBLE);
        if (currentSection == Section.SAVED) {
            applyFilterAndRender();
            refreshMap();
        } else if (currentSection == Section.RESULTS) {
            applyFilterAndRender();
        }
    }

    @Override
    public void showSearchHistory(List<HistoryItem> history) {
        historyAdapter.submitList(history);
        boolean empty = history == null || history.isEmpty();
        binding.searchOverlay.emptyHistory.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.searchOverlay.rvHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.searchOverlay.btnClearHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void showProfileSummary(String fullName, String role, String email, int savedCount, int historyCount) {
        String displayName = (fullName == null || fullName.trim().isEmpty()) ? "Utilisateur" : fullName.trim();
        String displayRole = (role == null || role.trim().isEmpty())
                ? "Utilisateur"
                : role.substring(0, 1).toUpperCase(Locale.getDefault()) + role.substring(1).toLowerCase(Locale.getDefault());

        binding.profileOverlay.tvProfileUsername.setText(displayName);
        binding.profileOverlay.tvMiniName.setText(displayName);
        binding.profileOverlay.tvMiniRole.setText(displayRole);
        binding.profileOverlay.tvProfileSubtitle.setText(getString(R.string.profile_summary_counts, savedCount, historyCount));
        binding.profileOverlay.tvProfileEmail.setText(email == null || email.isEmpty() ? getString(R.string.profile_email_placeholder) : email);
    }

    @Override
    public void showParkingDetails(ParkingUiModel item) {
        currentSection = Section.DETAIL;
        binding.searchOverlayContainer.setVisibility(View.GONE);
        binding.profileOverlayContainer.setVisibility(View.GONE);
        binding.bottomSheet.setVisibility(View.GONE);
        binding.btnSearchTop.setVisibility(View.GONE);
        binding.detailOverlayContainer.setVisibility(View.VISIBLE);

        binding.detailOverlay.tvDetailName.setText(item.getName());
        binding.detailOverlay.tvDetailAddress.setText(item.getAddress() == null || item.getAddress().isEmpty() ? "Adresse indisponible" : item.getAddress());
        binding.detailOverlay.tvDetailAvailability.setText(item.getAvailabilityText());
        binding.detailOverlay.tvDetailPrediction.setText(item.getPredictionText());
        Integer predicted = item.getPredictedFreePlaces();
        int predictionColor = predicted == null ? R.color.ps_text_muted : (predicted > 0 ? R.color.ps_success : R.color.ps_error);
        binding.detailOverlay.tvDetailPrediction.setTextColor(requireContext().getColor(predictionColor));
        binding.detailOverlay.tvDetailMeta.setText(item.getDetailsText());
        binding.detailOverlay.btnDetailSave.setText(item.isSaved() ? "Retirer des sauvegardes" : getString(R.string.detail_save));
        binding.detailOverlay.btnDetailSave.setTag(item);
        binding.detailOverlay.btnDetailRoute.setTag(item);

        focusMapOnParking(item);
    }

    private void focusMapOnParking(ParkingUiModel item) {
        if (item.getLatitude() == null || item.getLongitude() == null) {
            return;
        }
        centerMap(new LatLng(item.getLatitude(), item.getLongitude()), DETAIL_ZOOM);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onToggleSaved(ParkingUiModel item) {
        controller.toggleSaved(item);
    }

    @Override
    public void onShowDetails(ParkingUiModel item) {
        controller.loadParkingDetails(item);
    }

    @Override
    public void onHistoryClicked(HistoryItem item) {
        binding.searchOverlay.etSearch.setText(item.getDestinationText());
        setSelectedArrivalOption(item.getArrivalOption());
        if (item.getDestinationLat() != null && item.getDestinationLon() != null) {
            currentSearchCenter = new LatLng(item.getDestinationLat(), item.getDestinationLon());
            lastSearchLabel = item.getDestinationText() == null ? "Destination" : item.getDestinationText();
        }
        controller.onHistorySelected(item);
    }

    @Override
    public void onHistoryDeleted(HistoryItem item) {
        controller.deleteHistoryItem(item);
    }

    private void confirmClearHistory() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Supprimer l’historique")
                .setMessage("Voulez-vous supprimer tout l’historique de recherche ?")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Supprimer", (dialog, which) -> controller.clearHistory())
                .show();
    }

    private void confirmClearSaved() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Supprimer les sauvegardes")
                .setMessage("Voulez-vous supprimer tous les parkings sauvegardés ?")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Supprimer", (dialog, which) -> controller.clearSavedParkings())
                .show();
    }

    private void showArrivalChoiceDialog(ArrivalChoiceListener listener) {
        final String[] labels = {"+15 min", "+30 min", "+60 min"};
        final String[] values = {"plus_15", "plus_30", "plus_60"};
        int selectedIndex = 1;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choisissez votre heure d’arrivée")
                .setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> {
                    dialog.dismiss();
                    listener.onSelected(values[which]);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void startNearbySearch(String arrivalOption) {
        pendingLocationAction = () -> fetchCurrentLocation(location -> {
            if (location == null) {
                showMessage("Impossible de récupérer votre position actuelle.");
                return;
            }
            currentSearchCenter = new LatLng(location.getLatitude(), location.getLongitude());
            lastSearchLabel = "Autour de moi";
            setSelectedArrivalOption(arrivalOption);
            currentRoutePoints = null;
            currentRouteStart = currentSearchCenter;
            controller.search("Autour de moi", location.getLatitude(), location.getLongitude(), arrivalOption);
            refreshMap();
        });
        ensureLocationPermission();
    }

    private void drawSearchBaseState() {
        currentSection = Section.RESULTS;
        binding.tvSearchTop.setText(lastSearchLabel == null || lastSearchLabel.isEmpty() ? getString(R.string.home_search_placeholder) : lastSearchLabel);
        showResultsSection();
        refreshMap();
    }

    private void resolveAddressToCoordinates(String query, GeocodeResultListener listener) {
        executorService.execute(() -> {
            LatLng point = tryResolveWithAndroidGeocoder(query);
            LatLng finalPoint = point;
            handler.post(() -> {
                if (isAdded()) {
                    listener.onResult(finalPoint);
                }
            });
        });
    }

    private LatLng tryResolveWithAndroidGeocoder(String query) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private LatLng tryResolveWithGoogleGeocoding(String query) {
        try {
            String apiKey = getString(R.string.my_map_api_key);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return null;
            }
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encoded + "&key=" + apiKey;
            Request request = new Request.Builder().url(url).build();
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            JSONObject json = new JSONObject(response.body().string());
            JSONArray results = json.optJSONArray("results");
            if (results == null || results.length() == 0) {
                return null;
            }
            JSONObject location = results.getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONObject("location");
            return new LatLng(location.getDouble("lat"), location.getDouble("lng"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void ensureLocationPermission() {
        if (hasLocationPermission()) {
            if (pendingLocationAction != null) {
                Runnable action = pendingLocationAction;
                pendingLocationAction = null;
                action.run();
            }
            return;
        }
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation(LocationResultListener listener) {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(LocationManager.class);
        if (locationManager == null) {
            listener.onLocation(null);
            return;
        }
        String provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ? LocationManager.GPS_PROVIDER
                : LocationManager.NETWORK_PROVIDER;

        try {
            Location lastKnown = locationManager.getLastKnownLocation(provider);
            if (lastKnown != null) {
                listener.onLocation(lastKnown);
                return;
            }
        } catch (Exception ignored) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                locationManager.getCurrentLocation(provider, null, requireContext().getMainExecutor(), listener::onLocation);
                return;
            } catch (Exception ignored) {
            }
        }

        try {
            locationManager.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    listener.onLocation(location);
                }
            }, Looper.getMainLooper());
        } catch (Exception e) {
            listener.onLocation(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            boolean granted = false;
            for (int result : grantResults) {
                if (result == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            if (granted && pendingLocationAction != null) {
                Runnable action = pendingLocationAction;
                pendingLocationAction = null;
                action.run();
            } else {
                pendingLocationAction = null;
                showMessage("La localisation est nécessaire pour cette action.");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        if (mapView != null) {
            mapView.onDestroy();
        }
        executorService.shutdownNow();
        googleMap = null;
        mapView = null;
        binding = null;
    }

    private interface ArrivalChoiceListener {
        void onSelected(String arrivalOption);
    }

    private interface GeocodeResultListener {
        void onResult(@Nullable LatLng point);
    }

    private interface LocationResultListener {
        void onLocation(@Nullable Location location);
    }

    private interface RouteSuccessListener {
        void onRoute(List<LatLng> routePoints);
    }
}
