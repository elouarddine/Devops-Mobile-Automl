import json
import os
from typing import Optional
from urllib.parse import urlencode
from urllib.request import Request, urlopen


class GeocodingService:
    def __init__(self, timeout_seconds: int = 6):
        self.timeout_seconds = timeout_seconds

    def geocode(self, query: str) -> tuple[Optional[float], Optional[float]]:
        if not query or len(query.strip()) < 2:
            return None, None

        google_result = self._geocode_google(query)
        if google_result != (None, None):
            return google_result

        return self._geocode_nominatim(query)

    def _geocode_google(self, query: str) -> tuple[Optional[float], Optional[float]]:
        api_key = os.getenv("GOOGLE_MAPS_API_KEY") or os.getenv("GOOGLE_GEOCODING_API_KEY")
        if not api_key:
            return None, None

        params = urlencode({"address": query, "key": api_key})
        url = f"https://maps.googleapis.com/maps/api/geocode/json?{params}"
        try:
            request = Request(url, headers={"User-Agent": "ParkSmart/1.0"})
            with urlopen(request, timeout=self.timeout_seconds) as response:
                payload = json.loads(response.read().decode("utf-8"))
            results = payload.get("results") or []
            if not results:
                return None, None
            location = results[0].get("geometry", {}).get("location", {})
            lat = location.get("lat")
            lng = location.get("lng")
            return (float(lat), float(lng)) if lat is not None and lng is not None else (None, None)
        except Exception:
            return None, None

    def _geocode_nominatim(self, query: str) -> tuple[Optional[float], Optional[float]]:
        params = urlencode({"q": query, "format": "jsonv2", "limit": 1})
        url = f"https://nominatim.openstreetmap.org/search?{params}"
        try:
            request = Request(url, headers={"User-Agent": "ParkSmart/1.0"})
            with urlopen(request, timeout=self.timeout_seconds) as response:
                payload = json.loads(response.read().decode("utf-8"))
            if not payload:
                return None, None
            first = payload[0]
            return float(first["lat"]), float(first["lon"])
        except Exception:
            return None, None
