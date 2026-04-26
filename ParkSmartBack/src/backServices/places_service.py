import json
import math
import os
from urllib.parse import urlencode
from urllib.request import Request, urlopen


def _haversine(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    r = 6371000
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = math.sin(dlat / 2) ** 2 + math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlon / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


class GooglePlacesParkingService:
    """Recherche dynamique de parkings réels autour d'un point via Google Places Nearby Search."""

    def __init__(self, timeout_seconds: int = 8):
        self.timeout_seconds = timeout_seconds
        self.api_key = os.getenv("GOOGLE_MAPS_API_KEY") or os.getenv("GOOGLE_PLACES_API_KEY")

    def is_enabled(self) -> bool:
        return bool(self.api_key)

    def search_nearby_parkings(
        self,
        latitude: float,
        longitude: float,
        radius_m: int,
        arrival_option: str | None = None,
        limit: int = 12,
    ) -> list[dict]:
        if not self.api_key:
            return []

        raw_places = self._nearby_search(latitude, longitude, radius_m)
        results: list[dict] = []
        for item in raw_places[:limit]:
            geometry = item.get("geometry", {}).get("location", {})
            lat = geometry.get("lat")
            lon = geometry.get("lng")
            if lat is None or lon is None:
                continue

            distance_m = round(_haversine(latitude, longitude, float(lat), float(lon)))
            results.append(
                {
                    "parking_id": item.get("place_id") or f"google_{abs(hash((item.get('name'), lat, lon)))}",
                    "name": item.get("name") or "Parking",
                    "address": item.get("vicinity") or item.get("formatted_address") or item.get("name"),
                    "latitude": float(lat),
                    "longitude": float(lon),
                    "distance_m": distance_m,
                    "capacity": None,
                    "current_free_places": None,
                    "predicted_free_places": None,
                    "price_per_hour": None,
                    "source_name": "google_places",
                    "metadata": {
                        "rating": item.get("rating"),
                        "user_ratings_total": item.get("user_ratings_total"),
                        "business_status": item.get("business_status"),
                        "opening_hours": item.get("opening_hours"),
                    },
                }
            )
        return results

    def _nearby_search(self, latitude: float, longitude: float, radius_m: int) -> list[dict]:
        params = urlencode(
            {
                "location": f"{latitude},{longitude}",
                "radius": max(300, min(int(radius_m), 10000)),
                "type": "parking",
                "keyword": "parking",
                "key": self.api_key,
                "language": "fr",
            }
        )
        url = f"https://maps.googleapis.com/maps/api/place/nearbysearch/json?{params}"
        request = Request(url, headers={"User-Agent": "ParkSmart/1.0"})
        with urlopen(request, timeout=self.timeout_seconds) as response:
            payload = json.loads(response.read().decode("utf-8"))
        return payload.get("results") or []
