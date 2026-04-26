from fastapi import APIRouter, Depends

from src.api.dependencies.auth import get_current_user
from src.api.dto.search_dto import ParkingPredictionRequest, SearchRequest
from src.backServices.response_service import success_response
from src.backServices.search_service import SearchService
from src.storage.database import get_db
from src.storage.repositories.sqlalchemy_repositories import SQLAlchemyParkingRepository, SQLAlchemySearchHistoryRepository

router = APIRouter(tags=["search"])


def _service(db):
    return SearchService(SQLAlchemyParkingRepository(db), SQLAlchemySearchHistoryRepository(db))


@router.post("/search")
def search(payload: SearchRequest, current_user=Depends(get_current_user), db=Depends(get_db)):
    service = _service(db)
    result = service.search(
        user_id=current_user.id,
        destination_text=payload.destination_text,
        destination_lat=payload.destination_lat,
        destination_lon=payload.destination_lon,
        arrival_option=payload.arrival_option,
        radius_m=payload.radius_m,
    )
    return success_response(result)


@router.post("/parkings/predict")
def predict_selected_parking(payload: ParkingPredictionRequest, current_user=Depends(get_current_user), db=Depends(get_db)):
    service = _service(db)
    return success_response(service.predict_selected_parking(payload.dict(), payload.arrival_option))


@router.get("/parkings/{parking_id}")
def get_parking_details(parking_id: str, current_user=Depends(get_current_user), db=Depends(get_db)):
    from src.backServices.saved_parking_service import SavedParkingService
    from src.storage.repositories.sqlalchemy_repositories import SQLAlchemySavedParkingRepository

    service = SavedParkingService(SQLAlchemySavedParkingRepository(db), SQLAlchemyParkingRepository(db))
    return success_response(service.get_parking_details(parking_id))
