from typing import Any, Literal

from pydantic import BaseModel, Field


class SearchRequest(BaseModel):
    destination_text: str = Field(min_length=2, max_length=255)
    destination_lat: float | None = None
    destination_lon: float | None = None
    arrival_option: Literal["now", "plus_15", "plus_30", "plus_60"] = "plus_30"
    radius_m: int = Field(default=2000, ge=100, le=10000)


class SaveSearchRequest(BaseModel):
    title: str = Field(min_length=2, max_length=150)
    destination_text: str = Field(min_length=2, max_length=255)
    arrival_option: str = Field(min_length=2, max_length=50)
    recommended_parking_name: str | None = Field(default=None, max_length=150)
    payload: dict | None = None


class SaveParkingRequest(BaseModel):
    parking_id: str = Field(min_length=1, max_length=120)
    name: str = Field(min_length=2, max_length=150)
    address: str | None = Field(default=None, max_length=255)
    latitude: float | None = None
    longitude: float | None = None
    distance_m: int | None = Field(default=None, ge=0)
    capacity: int | None = Field(default=None, ge=0)
    current_free_places: int | None = Field(default=None, ge=0)
    predicted_free_places: int | None = Field(default=None, ge=0)
    price_per_hour: float | None = Field(default=None, ge=0)
    arrival_option: str | None = Field(default=None, max_length=50)
    metadata: dict | None = None


class ParkingPredictionRequest(BaseModel):
    parking_id: str = Field(min_length=1, max_length=120)
    name: str = Field(min_length=2, max_length=150)
    address: str | None = Field(default=None, max_length=255)
    latitude: float | None = None
    longitude: float | None = None
    distance_m: int | None = Field(default=None, ge=0)
    capacity: int | None = Field(default=None, ge=0)
    current_free_places: int | None = Field(default=None, ge=0)
    price_per_hour: float | None = Field(default=None, ge=0)
    arrival_option: Literal["now", "plus_15", "plus_30", "plus_60"] = "plus_30"
    source_name: str | None = Field(default=None, max_length=120)
    metadata: dict[str, Any] | None = None
