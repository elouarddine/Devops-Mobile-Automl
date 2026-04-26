import hashlib
from datetime import datetime
from pathlib import Path

try:
    import joblib
except Exception:  # pragma: no cover
    joblib = None

try:
    import numpy as np
except Exception:  # pragma: no cover
    np = None

try:
    import pandas as pd
except Exception:  # pragma: no cover
    pd = None

try:
    import openmeteo_requests
    import requests_cache
    from retry_requests import retry
except Exception:  # pragma: no cover
    openmeteo_requests = None
    requests_cache = None
    retry = None

try:
    from .actuel_data import get_best_current_data
except ImportError:  # pragma: no cover
    from actuel_data import get_best_current_data


class DummyParkingAvailabilityModel:
    name = "DummyParkingAvailabilityModel_v1"

    def predict(self, X):
        if pd is not None and hasattr(X, "columns") and "place_diponible" in X.columns:
            series = X["place_diponible"].fillna(0).astype(float) > 0
            return series.astype(int).to_numpy()
        value = X.get("place_diponible", 0) if isinstance(X, dict) else 0
        return [1 if float(value or 0) > 0 else 0]


def _safe_float(value):
    try:
        if value is None or value == "":
            return None
        return float(value)
    except Exception:
        return None


def _build_openmeteo_client():
    if openmeteo_requests is None or requests_cache is None or retry is None:
        return None
    try:
        cache_session = requests_cache.CachedSession(".cache", expire_after=3600)
        retry_session = retry(cache_session, retries=3, backoff_factor=0.2)
        return openmeteo_requests.Client(session=retry_session)
    except Exception:
        return None


OPEN_METEO_CLIENT = _build_openmeteo_client()
OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast"
MODELS_DIR = (Path(__file__).resolve().parent / "models").resolve()
_MODEL_CACHE = {}


def _select_model_filename(temps_min):
    t = int(temps_min)
    if t <= 15:
        return "model_15min.joblib"
    if t <= 30:
        return "model_30min.joblib"
    return "model_1h.joblib"


def _extract_model(loaded_obj, model_file):
    if isinstance(loaded_obj, dict) and "pipeline" in loaded_obj:
        model = loaded_obj["pipeline"]
        best_name = loaded_obj.get("best_model_name")
        if not hasattr(model, "name"):
            model.name = str(best_name) if best_name else str(model_file)
        return model
    return loaded_obj


def _prediction_to_bool(y_pred):
    val = y_pred[0] if isinstance(y_pred, (list, tuple)) else y_pred
    if np is not None:
        try:
            val = np.asarray(y_pred).reshape(-1)[0]
        except Exception:
            pass
    if isinstance(val, str):
        norm = val.strip().lower()
        if norm in ("oui", "yes", "true", "1"):
            return True
        if norm in ("non", "no", "false", "0"):
            return False
    try:
        return bool(int(float(val)))
    except Exception:
        return bool(val)


def get_weather_features(latitude, longitude, temps_min):
    date_str = datetime.now().strftime("%Y-%m-%d")
    if OPEN_METEO_CLIENT is None or pd is None or np is None:
        return {
            "date": date_str,
            "target_time_utc": str(datetime.utcnow()),
            "temperature": 18,
            "pluie": 0,
        }

    try:
        params = {
            "latitude": float(latitude),
            "longitude": float(longitude),
            "hourly": ["temperature_2m", "rain"],
            "start_date": date_str,
            "end_date": date_str,
        }
        responses = OPEN_METEO_CLIENT.weather_api(OPEN_METEO_URL, params=params)
        if not responses:
            raise RuntimeError("Aucune reponse Open-Meteo.")

        response = responses[0]
        hourly = response.Hourly()
        hourly_temperature = hourly.Variables(0).ValuesAsNumpy()
        hourly_rain = hourly.Variables(1).ValuesAsNumpy()

        hourly_dates = pd.date_range(
            start=pd.to_datetime(hourly.Time(), unit="s", utc=True),
            end=pd.to_datetime(hourly.TimeEnd(), unit="s", utc=True),
            freq=pd.Timedelta(seconds=hourly.Interval()),
            inclusive="left",
        )

        target_dt = pd.Timestamp.utcnow() + pd.Timedelta(minutes=int(temps_min))
        idx = int(np.argmin(np.abs(hourly_dates - target_dt)))

        temperature = int(round(float(hourly_temperature[idx])))
        pluie = 1 if float(hourly_rain[idx]) > 0 else 0
        return {
            "date": date_str,
            "target_time_utc": str(target_dt),
            "temperature": temperature,
            "pluie": int(pluie),
        }
    except Exception:
        return {
            "date": date_str,
            "target_time_utc": str(datetime.utcnow()),
            "temperature": 18,
            "pluie": 0,
        }


def load_model(temps_min, model_path=None):
    if joblib is None:
        return DummyParkingAvailabilityModel()

    if model_path is None:
        model_file = _select_model_filename(temps_min)
        model_path = MODELS_DIR / model_file
    else:
        model_path = Path(model_path).resolve()
        model_file = model_path.name

    if not model_path.exists():
        return DummyParkingAvailabilityModel()

    cache_key = str(model_path)
    if cache_key in _MODEL_CACHE:
        return _MODEL_CACHE[cache_key]

    try:
        loaded_obj = joblib.load(model_path)
        model = _extract_model(loaded_obj, model_file=model_file)
        if not hasattr(model, "predict"):
            return DummyParkingAvailabilityModel()
        if not hasattr(model, "name"):
            model.name = str(model_file)
        _MODEL_CACHE[cache_key] = model
        return model
    except Exception:
        return DummyParkingAvailabilityModel()


def _build_base_row(latitude, longitude, adresse=None, current=None):
    return {
        "nom": (current or {}).get("nom") or (adresse or "Parking"),
        "adresse": (current or {}).get("adresse") or (adresse or "Adresse inconnue"),
        "longitude": float(longitude),
        "latitude": float(latitude),
        "capacite_max": _safe_float((current or {}).get("voitureplacescapacite")) or 100.0,
    }


def build_prediction_input(base_row, current_row, weather):
    nb_disponible = _safe_float((current_row or {}).get("voitureplacesdisponibles"))
    if nb_disponible is None:
        nb_disponible = 0.0

    if pd is None:
        return {
            "longitude": float(base_row["longitude"]),
            "latitude": float(base_row["latitude"]),
            "capacite_max": float(base_row["capacite_max"]),
            "place_diponible": float(nb_disponible),
            "temperature": int(weather["temperature"]),
            "pleu": int(weather["pluie"]),
        }

    ordered_columns = [
        "longitude",
        "latitude",
        "capacite_max",
        "place_diponible",
        "temperature",
        "pleu",
    ]

    features = pd.DataFrame(
        [
            {
                "longitude": float(base_row["longitude"]),
                "latitude": float(base_row["latitude"]),
                "capacite_max": float(base_row["capacite_max"]),
                "place_diponible": float(nb_disponible),
                "temperature": int(weather["temperature"]),
                "pleu": int(weather["pluie"]),
            }
        ],
        columns=ordered_columns,
    )
    return features


def _fallback_prediction(latitude, longitude, temps_min, adresse=None):
    key = f"{round(float(latitude), 4)}|{round(float(longitude), 4)}|{int(temps_min)}|{adresse or ''}"
    digest = hashlib.sha256(key.encode("utf-8")).hexdigest()
    return int(digest[:2], 16) % 2 == 0


def run_prediction(latitude, longitude, temps_min, adresse=None, model=None):
    weather = get_weather_features(latitude=latitude, longitude=longitude, temps_min=temps_min)

    current = get_best_current_data(
        latitude=latitude,
        longitude=longitude,
        adresse=adresse,
    )

    base_row = _build_base_row(latitude=latitude, longitude=longitude, adresse=adresse, current=current)
    selected_model = model if model is not None else load_model(temps_min=temps_min)
    X = build_prediction_input(base_row=base_row, current_row=current or {}, weather=weather)

    try:
        y_pred = selected_model.predict(X)
        available = _prediction_to_bool(y_pred)
    except Exception:
        try:
            if pd is not None and hasattr(X, "rename"):
                X_legacy = X.rename(columns={"place_diponible": "nb_disponible", "pleu": "plui"})
                y_pred = selected_model.predict(X_legacy)
                available = _prediction_to_bool(y_pred)
            else:
                raise RuntimeError("Fallback")
        except Exception:
            available = _fallback_prediction(latitude, longitude, temps_min, adresse)

    return {
        "request": {
            "latitude": float(latitude),
            "longitude": float(longitude),
            "temps": int(temps_min),
            "date": weather["date"],
        },
        "response": {
            "nom": str(base_row.get("nom", "")),
            "adresse": str(base_row.get("adresse", "")),
            "longitude": float(base_row["longitude"]),
            "latitude": float(base_row["latitude"]),
            "prediction": bool(available),
        },
        "debug": {
            "model": getattr(selected_model, "name", selected_model.__class__.__name__),
            "model_file": _select_model_filename(temps_min),
            "features": X.iloc[0].to_dict() if pd is not None and hasattr(X, "iloc") else X,
            "weather": {
                "temperature": int(weather["temperature"]),
                "pleu": int(weather["pluie"]),
                "target_time_utc": weather["target_time_utc"],
            },
        },
    }
