from src.api.errors.exceptions import NotFoundError


class HistoryService:
    def __init__(self, history_repository):
        self.history_repository = history_repository

    def get_user_history(self, user_id: int) -> list[dict]:
        items = self.history_repository.find_by_user_id(user_id)
        return [
            {
                "id": item.id,
                "searched_at": item.searched_at.isoformat(),
                "destination_text": item.destination_text,
                "destination_lat": item.destination_lat,
                "destination_lon": item.destination_lon,
                "arrival_option": item.arrival_option,
                "radius_m": item.radius_m,
                "recommended_parking": item.recommended_parking_name,
                "results": item.results_json or [],
            }
            for item in items
        ]

    def delete_history_item(self, user_id: int, history_id: int) -> None:
        deleted = self.history_repository.delete_by_user_and_id(user_id, history_id)
        if not deleted:
            raise NotFoundError("Élément d'historique introuvable", code="HISTORY_ITEM_NOT_FOUND")

    def clear_history(self, user_id: int) -> int:
        return self.history_repository.delete_all_by_user_id(user_id)
