from fastapi import APIRouter, Depends, status

from src.api.dependencies.auth import get_current_user
from src.api.dto.search_dto import SaveParkingRequest, SaveSearchRequest
from src.backServices.history_service import HistoryService
from src.backServices.response_service import success_response
from src.backServices.saved_parking_service import SavedParkingService
from src.backServices.saved_search_service import SavedSearchService
from src.storage.database import get_db
from src.storage.repositories.sqlalchemy_repositories import (
    SQLAlchemyParkingRepository,
    SQLAlchemySavedParkingRepository,
    SQLAlchemySavedSearchRepository,
    SQLAlchemySearchHistoryRepository,
)

router = APIRouter(tags=["history"])


@router.get("/history")
def get_history(current_user=Depends(get_current_user), db=Depends(get_db)):
    service = HistoryService(SQLAlchemySearchHistoryRepository(db))
    return success_response({"items": service.get_user_history(current_user.id)})


@router.delete("/history/{history_id}")
def delete_history_item(history_id: int, current_user=Depends(get_current_user), db=Depends(get_db)):
    service = HistoryService(SQLAlchemySearchHistoryRepository(db))
    service.delete_history_item(current_user.id, history_id)
    return success_response({"deleted": True, "history_id": history_id})


@router.delete("/history")
def clear_history(current_user=Depends(get_current_user), db=Depends(get_db)):
    service = HistoryService(SQLAlchemySearchHistoryRepository(db))
    deleted = service.clear_history(current_user.id)
    return success_response({"deleted": True, "count": deleted})


@router.post("/saved-searches", status_code=status.HTTP_201_CREATED)
def save_search(payload: SaveSearchRequest, current_user=Depends(get_current_user), db=Depends(get_db)):
    service = SavedSearchService(SQLAlchemySavedSearchRepository(db))
    item = service.create_saved_search(
        user_id=current_user.id,
        title=payload.title,
        destination_text=payload.destination_text,
        arrival_option=payload.arrival_option,
        recommended_parking_name=payload.recommended_parking_name,
        payload=payload.payload,
    )
    return success_response({"saved_search_id": item.id})


@router.get("/saved-searches")
def get_saved_searches(current_user=Depends(get_current_user), db=Depends(get_db)):
    service = SavedSearchService(SQLAlchemySavedSearchRepository(db))
    return success_response({"items": service.list_saved_searches(current_user.id)})


@router.post("/saved-parkings", status_code=status.HTTP_201_CREATED)
def save_parking(payload: SaveParkingRequest, current_user=Depends(get_current_user), db=Depends(get_db)):
    service = SavedParkingService(SQLAlchemySavedParkingRepository(db), SQLAlchemyParkingRepository(db))
    item = service.save_parking(
        user_id=current_user.id,
        parking_external_id=payload.parking_id,
        parking_name=payload.name,
        address=payload.address,
        latitude=payload.latitude,
        longitude=payload.longitude,
        distance_m=payload.distance_m,
        capacity=payload.capacity,
        current_free_places=payload.current_free_places,
        predicted_free_places=payload.predicted_free_places,
        price_per_hour=payload.price_per_hour,
        arrival_option=payload.arrival_option,
        metadata=payload.metadata,
    )
    return success_response({"saved_parking_id": item.id})


@router.get("/saved-parkings")
def get_saved_parkings(current_user=Depends(get_current_user), db=Depends(get_db)):
    service = SavedParkingService(SQLAlchemySavedParkingRepository(db), SQLAlchemyParkingRepository(db))
    return success_response({"items": service.list_saved_parkings(current_user.id)})


@router.delete("/saved-parkings/{parking_id}")
def delete_saved_parking(parking_id: str, current_user=Depends(get_current_user), db=Depends(get_db)):
    service = SavedParkingService(SQLAlchemySavedParkingRepository(db), SQLAlchemyParkingRepository(db))
    service.delete_saved_parking(current_user.id, parking_id)
    return success_response({"deleted": True, "parking_id": parking_id})


@router.delete("/saved-parkings")
def clear_saved_parkings(current_user=Depends(get_current_user), db=Depends(get_db)):
    service = SavedParkingService(SQLAlchemySavedParkingRepository(db), SQLAlchemyParkingRepository(db))
    deleted = service.clear_saved_parkings(current_user.id)
    return success_response({"deleted": True, "count": deleted})
