import requests

URL = "https://data.nantesmetropole.fr/api/explore/v2.1/catalog/datasets/244400404_parcs-relais-nantes-metropole-disponibilites/records?where=grp_statut%3D5&limit=-1"


def _safe_float(value):
    try:
        if value is None or value == "":
            return None
        return float(value)
    except Exception:
        return None


def _normalize_text(value):
    if value is None:
        return ""
    return str(value).strip().lower()


def fetch_data(timeout=10):
    try:
        response = requests.get(URL, timeout=timeout)
        response.raise_for_status()
        data = response.json()
        return data.get("results", [])
    except Exception:
        return []


def process_data(records):
    processed = []
    for rec in records:
        processed.append({
            "datemaj": rec.get("grp_horodatage"),
            "nom": rec.get("grp_nom"),
            "adresse": rec.get("adresse"),
            "voitureplacescapacite": _safe_float(rec.get("grp_exploitation")),
            "voitureplacesdisponibles": _safe_float(rec.get("grp_disponible")),
            "longitude": _safe_float(rec.get("location", {}).get("lon")),
            "latitude": _safe_float(rec.get("location", {}).get("lat")),
            "places_disponibles": rec.get("places_disponibles")
        })
    return processed


def _distance_squared(lat1, lon1, lat2, lon2):
    return (lat1 - lat2) ** 2 + (lon1 - lon2) ** 2


def find_best_record(records, latitude=None, longitude=None, adresse=None):
    if not records:
        return None

    addr_query = _normalize_text(adresse)
    if addr_query:
        for rec in records:
            if addr_query in _normalize_text(rec.get("adresse")):
                return rec
            if addr_query in _normalize_text(rec.get("nom")):
                return rec

    lat = _safe_float(latitude)
    lon = _safe_float(longitude)
    if lat is None or lon is None:
        return None

    best_rec = None
    best_dist = None
    for rec in records:
        rlat = _safe_float(rec.get("latitude"))
        rlon = _safe_float(rec.get("longitude"))
        if rlat is None or rlon is None:
            continue
        dist = _distance_squared(lat, lon, rlat, rlon)
        if best_dist is None or dist < best_dist:
            best_dist = dist
            best_rec = rec

    return best_rec


def get_best_current_data(latitude=None, longitude=None, adresse=None, timeout=10):
    records = fetch_data(timeout=timeout)
    processed = process_data(records)
    return find_best_record(processed, latitude=latitude, longitude=longitude, adresse=adresse)
