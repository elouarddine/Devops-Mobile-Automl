from __future__ import annotations

import random

from src.predictor.api_predict import run_prediction


class PredictionService:
    def __init__(self):
        self._minutes_by_arrival = {
            "now": 15,
            "plus_15": 15,
            "plus_30": 30,
            "plus_60": 60,
        }

    def predict_availability(self, latitude: float | None, longitude: float | None, arrival_option: str | None, address: str | None = None) -> bool | None:
        result = self.predict_availability_with_fallback(
            latitude=latitude,
            longitude=longitude,
            arrival_option=arrival_option,
            address=address,
        )
        available = result.get("available")
        return None if available is None else bool(available)

    def predict_availability_with_fallback(
        self,
        latitude: float | None,
        longitude: float | None,
        arrival_option: str | None,
        address: str | None = None,
    ) -> dict:
        minutes = self._minutes_by_arrival.get(arrival_option or "")
        if latitude is None or longitude is None:
            return self._fallback_result("missing_coordinates", arrival_option, address)
        if minutes is None:
            return self._fallback_result("invalid_arrival_option", arrival_option, address)

        try:
            result = run_prediction(
                latitude=float(latitude),
                longitude=float(longitude),
                temps_min=int(minutes),
                adresse=address,
            )
            response = result.get("response") or {}
            prediction = response.get("prediction")
            if prediction is None:
                return self._fallback_result("empty_prediction", arrival_option, address)

            return {
                "available": bool(prediction),
                "predicted_free_places": 1 if bool(prediction) else 0,
                "fallback_used": False,
                "reason": "automl_success",
                "minutes": int(minutes),
                "raw": result,
            }
        except Exception as exc:
            return self._fallback_result("prediction_exception", arrival_option, address, details=str(exc))

    def _fallback_result(self, reason: str, arrival_option: str | None, address: str | None = None, details: str | None = None) -> dict:
        available = random.choice([True, False])
        minutes = self._minutes_by_arrival.get(arrival_option or "")
        payload = {
            "available": available,
            "predicted_free_places": 1 if available else 0,
            "fallback_used": True,
            "reason": reason,
            "minutes": minutes,
            "raw": {
                "address": address,
                "details": details,
            },
        }
        return payload
