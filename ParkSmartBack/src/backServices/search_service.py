from src.api.errors.exceptions import BadRequestError
from src.backServices.geocoding_service import GeocodingService
from src.backServices.places_service import GooglePlacesParkingService
from src.backServices.prediction_service import PredictionService
from src.storage.db_models.tables import SearchHistory


class SearchService:
    def __init__(self, parking_repository, history_repository, geocoding_service: GeocodingService | None = None, places_service: GooglePlacesParkingService | None = None, prediction_service: PredictionService | None = None):
        self.parking_repository = parking_repository
        self.history_repository = history_repository
        self.geocoding_service = geocoding_service or GeocodingService()
        self.places_service = places_service or GooglePlacesParkingService()
        self.prediction_service = prediction_service or PredictionService()

    def search(
        self,
        user_id: int,
        destination_text: str,
        destination_lat: float | None,
        destination_lon: float | None,
        arrival_option: str,
        radius_m: int,
    ) -> dict:
        if destination_lat is None or destination_lon is None:
            destination_lat, destination_lon = self.geocoding_service.geocode(destination_text)

        if destination_lat is None or destination_lon is None:
            raise BadRequestError("Impossible de géocoder cette adresse", code="GEOCODING_FAILED")

        normalized_arrival = self._normalize_arrival_option(arrival_option)

        dynamic_results = []
        if self.places_service.is_enabled():
            try:
                dynamic_results = self.places_service.search_nearby_parkings(
                    destination_lat,
                    destination_lon,
                    radius_m,
                    normalized_arrival,
                )
            except Exception:
                dynamic_results = []

        local_parkings = self.parking_repository.find_within_radius(destination_lat, destination_lon, radius_m)
        if not local_parkings and hasattr(self.parking_repository, "find_nearest"):
            local_parkings = self.parking_repository.find_nearest(destination_lat, destination_lon, limit=5)

        local_results = []
        for parking in local_parkings:
            local_results.append(
                {
                    "parking_id": parking.external_id or f"parking_{parking.id}",
                    "name": parking.name,
                    "address": parking.address,
                    "latitude": parking.latitude,
                    "longitude": parking.longitude,
                    "distance_m": getattr(parking, "computed_distance_m", None),
                    "capacity": parking.capacity,
                    "current_free_places": parking.current_free_places,
                    "predicted_free_places": None,
                    "price_per_hour": parking.price_per_hour,
                    "source_name": parking.source_name or "seed",
                    "metadata": parking.metadata_json or {},
                }
            )

        results = self._merge_results(dynamic_results, local_results)

        recommended = None
        best = None
        if results:
            best = sorted(
                results,
                key=lambda item: (item.get("distance_m") or 999999, item.get("price_per_hour") or 999999, item.get("name") or ""),
            )[0]
            recommended = {
                "parking_id": best["parking_id"],
                "reason": "distance",
            }

        history = SearchHistory(
            user_id=user_id,
            destination_text=destination_text,
            destination_lat=destination_lat,
            destination_lon=destination_lon,
            arrival_option=normalized_arrival,
            radius_m=radius_m,
            recommended_parking_name=best["name"] if best else None,
            results_json=results,
        )
        self.history_repository.save(history)

        return {
            "arrival_option": normalized_arrival,
            "destination": {
                "text": destination_text,
                "latitude": destination_lat,
                "longitude": destination_lon,
            },
            "recommended": recommended,
            "results": results,
        }

    def predict_selected_parking(self, parking_payload: dict, arrival_option: str | None) -> dict:
        if not isinstance(parking_payload, dict):
            raise BadRequestError("Les données du parking sont invalides", code="INVALID_PARKING_PAYLOAD")

        parking_id = parking_payload.get("parking_id")
        name = parking_payload.get("name") or "Parking"
        address = parking_payload.get("address")
        latitude = parking_payload.get("latitude")
        longitude = parking_payload.get("longitude")
        capacity = parking_payload.get("capacity")
        current_free_places = parking_payload.get("current_free_places")
        price_per_hour = parking_payload.get("price_per_hour")
        source_name = parking_payload.get("source_name") or "search_result"
        distance_m = parking_payload.get("distance_m")
        metadata = parking_payload.get("metadata") or {}

        prediction = self.prediction_service.predict_availability_with_fallback(
            latitude=latitude,
            longitude=longitude,
            arrival_option=self._normalize_arrival_option(arrival_option),
            address=address or name,
        )

        return {
            "parking_id": parking_id,
            "name": name,
            "address": address,
            "latitude": latitude,
            "longitude": longitude,
            "capacity": capacity,
            "current_free_places": current_free_places,
            "predicted_free_places": prediction.get("predicted_free_places"),
            "price_per_hour": price_per_hour,
            "source_name": source_name,
            "distance_m": distance_m,
            "arrival_option": self._normalize_arrival_option(arrival_option),
            "prediction_fallback_used": prediction.get("fallback_used", False),
            "prediction_reason": prediction.get("reason"),
            "metadata": metadata,
        }

    @staticmethod
    def _merge_results(dynamic_results: list[dict], local_results: list[dict]) -> list[dict]:
        merged: list[dict] = []
        seen: set[str] = set()

        for source in (dynamic_results or []) + (local_results or []):
            lat = source.get("latitude")
            lon = source.get("longitude")
            name = (source.get("name") or "").strip().lower()
            key = f"{name}|{round(lat or 0, 4)}|{round(lon or 0, 4)}"
            if key in seen:
                continue
            seen.add(key)
            merged.append(source)

        merged.sort(key=lambda item: (item.get("distance_m") or 999999, item.get("name") or ""))
        return merged[:12]

    @staticmethod
    def _normalize_arrival_option(arrival_option: str | None) -> str:
        if arrival_option == "plus_15":
            return "plus_15"
        if arrival_option == "plus_60":
            return "plus_60"
        if arrival_option == "now":
            return "now"
        return "plus_30"
