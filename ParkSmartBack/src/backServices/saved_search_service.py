from src.storage.db_models.tables import SavedSearch


class SavedSearchService:
    def __init__(self, saved_search_repository):
        self.saved_search_repository = saved_search_repository

    def create_saved_search(
        self,
        user_id: int,
        title: str,
        destination_text: str,
        arrival_option: str,
        recommended_parking_name: str | None,
        payload: dict | None,
    ) -> SavedSearch:
        entity = SavedSearch(
            user_id=user_id,
            title=title,
            destination_text=destination_text,
            arrival_option=arrival_option,
            recommended_parking_name=recommended_parking_name,
            payload_json=payload,
        )
        return self.saved_search_repository.save(entity)

    def list_saved_searches(self, user_id: int) -> list[dict]:
        items = self.saved_search_repository.find_by_user_id(user_id)
        return [
            {
                "id": item.id,
                "title": item.title,
                "destination_text": item.destination_text,
                "arrival_option": item.arrival_option,
                "recommended_parking_name": item.recommended_parking_name,
                "created_at": item.created_at.isoformat(),
            }
            for item in items
        ]
