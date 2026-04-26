from math import asin, cos, radians, sin, sqrt
from typing import Optional

from src.storage.contracts import (
    DatasetRepository,
    MLJobRepository,
    MLResultRepository,
    ParkingRepository,
    SavedSearchRepository,
    SavedParkingRepository,
    SearchHistoryRepository,
    UserRepository,
)
from src.storage.db_models.enums import JobStatus
from src.storage.db_models.tables import DatasetImport, MLJob, MLResult, Parking, SavedParking, SavedSearch, SearchHistory, User
from .sqlalchemy_base_repository import SQLAlchemyBaseRepository


def _haversine(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    r = 6371000
    dlat = radians(lat2 - lat1)
    dlon = radians(lon2 - lon1)
    a = sin(dlat / 2) ** 2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(dlon / 2) ** 2
    return 2 * r * asin(sqrt(a))


class SQLAlchemyUserRepository(SQLAlchemyBaseRepository[User], UserRepository):
    def __init__(self, session):
        super().__init__(session, User)

    def find_by_email(self, email: str) -> Optional[User]:
        return self.find_one_by(email=email)


class SQLAlchemyParkingRepository(SQLAlchemyBaseRepository[Parking], ParkingRepository):
    def __init__(self, session):
        super().__init__(session, Parking)

    def find_within_radius(self, latitude: float, longitude: float, radius_m: int) -> list[Parking]:
        all_parkings = self.session.query(Parking).all()
        items = []
        for parking in all_parkings:
            distance = _haversine(latitude, longitude, parking.latitude, parking.longitude)
            if distance <= radius_m:
                setattr(parking, "computed_distance_m", round(distance))
                items.append(parking)
        items.sort(key=lambda p: getattr(p, "computed_distance_m", 999999))
        return items

    def find_by_external_id(self, parking_external_id: str) -> Optional[Parking]:
        return self.find_one_by(external_id=parking_external_id)

    def find_nearest(self, latitude: float, longitude: float, limit: int = 5) -> list[Parking]:
        all_parkings = self.session.query(Parking).all()
        items = []
        for parking in all_parkings:
            distance = _haversine(latitude, longitude, parking.latitude, parking.longitude)
            setattr(parking, "computed_distance_m", round(distance))
            items.append(parking)
        items.sort(key=lambda p: getattr(p, "computed_distance_m", 999999))
        return items[:limit]


class SQLAlchemySearchHistoryRepository(SQLAlchemyBaseRepository[SearchHistory], SearchHistoryRepository):
    def __init__(self, session):
        super().__init__(session, SearchHistory)

    def find_by_user_id(self, user_id: int) -> list[SearchHistory]:
        return (
            self.session.query(SearchHistory)
            .filter(SearchHistory.user_id == user_id)
            .order_by(SearchHistory.searched_at.desc())
            .all()
        )

    def delete_by_user_and_id(self, user_id: int, history_id: int) -> bool:
        entity = (
            self.session.query(SearchHistory)
            .filter(SearchHistory.user_id == user_id, SearchHistory.id == history_id)
            .first()
        )
        if not entity:
            return False
        self.delete(entity)
        return True

    def delete_all_by_user_id(self, user_id: int) -> int:
        count = self.session.query(SearchHistory).filter(SearchHistory.user_id == user_id).delete()
        self.session.commit()
        return count


class SQLAlchemySavedSearchRepository(SQLAlchemyBaseRepository[SavedSearch], SavedSearchRepository):
    def __init__(self, session):
        super().__init__(session, SavedSearch)

    def find_by_user_id(self, user_id: int) -> list[SavedSearch]:
        return (
            self.session.query(SavedSearch)
            .filter(SavedSearch.user_id == user_id)
            .order_by(SavedSearch.created_at.desc())
            .all()
        )


class SQLAlchemySavedParkingRepository(SQLAlchemyBaseRepository[SavedParking], SavedParkingRepository):
    def __init__(self, session):
        super().__init__(session, SavedParking)

    def find_by_user_id(self, user_id: int) -> list[SavedParking]:
        return (
            self.session.query(SavedParking)
            .filter(SavedParking.user_id == user_id)
            .order_by(SavedParking.saved_at.desc())
            .all()
        )

    def find_by_user_and_external_id(self, user_id: int, parking_external_id: str) -> Optional[SavedParking]:
        return (
            self.session.query(SavedParking)
            .filter(SavedParking.user_id == user_id, SavedParking.parking_external_id == parking_external_id)
            .first()
        )

    def delete_by_user_and_external_id(self, user_id: int, parking_external_id: str) -> bool:
        entity = self.find_by_user_and_external_id(user_id, parking_external_id)
        if not entity:
            return False
        self.delete(entity)
        return True

    def delete_all_by_user_id(self, user_id: int) -> int:
        count = self.session.query(SavedParking).filter(SavedParking.user_id == user_id).delete()
        self.session.commit()
        return count


class SQLAlchemyDatasetRepository(SQLAlchemyBaseRepository[DatasetImport], DatasetRepository):
    def __init__(self, session):
        super().__init__(session, DatasetImport)

    def find_active(self) -> Optional[DatasetImport]:
        return (
            self.session.query(DatasetImport)
            .filter(DatasetImport.is_active.is_(True))
            .order_by(DatasetImport.imported_at.desc())
            .first()
        )


class SQLAlchemyMLJobRepository(SQLAlchemyBaseRepository[MLJob], MLJobRepository):
    def __init__(self, session):
        super().__init__(session, MLJob)

    def find_running_job(self) -> Optional[MLJob]:
        return self.find_one_by(status=JobStatus.RUNNING)

    def find_all_ordered(self) -> list[MLJob]:
        return self.session.query(MLJob).order_by(MLJob.started_at.desc()).all()


class SQLAlchemyMLResultRepository(SQLAlchemyBaseRepository[MLResult], MLResultRepository):
    def __init__(self, session):
        super().__init__(session, MLResult)

    def find_latest_by_mode(self, mode: str) -> Optional[MLResult]:
        return (
            self.session.query(MLResult)
            .filter(MLResult.mode == mode)
            .order_by(MLResult.created_at.desc())
            .first()
        )
