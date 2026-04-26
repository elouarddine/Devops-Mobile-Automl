from src.api.errors.exceptions import ConflictError, NotFoundError
from src.storage.db_models.tables import SavedParking


class SavedParkingService:
    def __init__(self, saved_parking_repository, parking_repository):
        self.saved_parking_repository = saved_parking_repository
        self.parking_repository = parking_repository

    def save_parking(
        self,
        user_id: int,
        parking_external_id: str,
        parking_name: str,
        address: str | None,
        latitude: float | None,
        longitude: float | None,
        distance_m: int | None,
        capacity: int | None,
        current_free_places: int | None,
        predicted_free_places: int | None,
        price_per_hour: float | None,
        arrival_option: str | None,
        metadata: dict | None,
    ) -> SavedParking:
        existing = self.saved_parking_repository.find_by_user_and_external_id(user_id, parking_external_id)
        if existing:
            raise ConflictError("Ce parking est déjà sauvegardé", code="PARKING_ALREADY_SAVED")

        entity = SavedParking(
            user_id=user_id,
            parking_external_id=parking_external_id,
            parking_name=parking_name,
            address=address,
            latitude=latitude,
            longitude=longitude,
            distance_m=distance_m,
            capacity=capacity,
            current_free_places=current_free_places,
            predicted_free_places=predicted_free_places,
            price_per_hour=price_per_hour,
            arrival_option=arrival_option,
            metadata_json=metadata,
        )
        return self.saved_parking_repository.save(entity)

    def delete_saved_parking(self, user_id: int, parking_external_id: str) -> None:
        deleted = self.saved_parking_repository.delete_by_user_and_external_id(user_id, parking_external_id)
        if not deleted:
            raise NotFoundError("Parking sauvegardé introuvable", code="SAVED_PARKING_NOT_FOUND")

    def clear_saved_parkings(self, user_id: int) -> int:
        return self.saved_parking_repository.delete_all_by_user_id(user_id)

    def list_saved_parkings(self, user_id: int) -> list[dict]:
        items = self.saved_parking_repository.find_by_user_id(user_id)
        return [
            {
                "parking_id": item.parking_external_id,
                "name": item.parking_name,
                "address": item.address,
                "latitude": item.latitude,
                "longitude": item.longitude,
                "distance_m": item.distance_m,
                "capacity": item.capacity,
                "current_free_places": item.current_free_places,
                "predicted_free_places": item.predicted_free_places,
                "price_per_hour": item.price_per_hour,
                "arrival_option": item.arrival_option,
                "saved_at": item.saved_at.isoformat(),
                "metadata": item.metadata_json or {},
            }
            for item in items
        ]

    def get_parking_details(self, parking_id: str) -> dict:
        parking = self.parking_repository.find_by_external_id(parking_id)
        if not parking:
            raise NotFoundError("Parking introuvable", code="PARKING_NOT_FOUND")

        return {
            "parking_id": parking.external_id or f"parking_{parking.id}",
            "name": parking.name,
            "address": parking.address,
            "latitude": parking.latitude,
            "longitude": parking.longitude,
            "capacity": parking.capacity,
            "current_free_places": parking.current_free_places,
            "price_per_hour": parking.price_per_hour,
            "source_name": parking.source_name,
            "metadata": parking.metadata_json or {},
        }
