from abc import abstractmethod
from typing import Optional

from .interface_base import BaseRepository
from src.storage.db_models.tables import (
    User,
    Parking,
    SearchHistory,
    SavedSearch,
    SavedParking,
    DatasetImport,
    MLJob,
    MLResult,
)


class UserRepository(BaseRepository[User]):
    @abstractmethod
    def find_by_email(self, email: str) -> Optional[User]: ...


class ParkingRepository(BaseRepository[Parking]):
    @abstractmethod
    def find_within_radius(self, latitude: float, longitude: float, radius_m: int) -> list[Parking]: ...


class SearchHistoryRepository(BaseRepository[SearchHistory]):
    @abstractmethod
    def find_by_user_id(self, user_id: int) -> list[SearchHistory]: ...


class SavedSearchRepository(BaseRepository[SavedSearch]):
    @abstractmethod
    def find_by_user_id(self, user_id: int) -> list[SavedSearch]: ...


class SavedParkingRepository(BaseRepository[SavedParking]):
    @abstractmethod
    def find_by_user_id(self, user_id: int) -> list[SavedParking]: ...

    @abstractmethod
    def find_by_user_and_external_id(self, user_id: int, parking_external_id: str) -> Optional[SavedParking]: ...

    @abstractmethod
    def delete_by_user_and_external_id(self, user_id: int, parking_external_id: str) -> bool: ...


class DatasetRepository(BaseRepository[DatasetImport]):
    @abstractmethod
    def find_active(self) -> Optional[DatasetImport]: ...


class MLJobRepository(BaseRepository[MLJob]):
    @abstractmethod
    def find_running_job(self) -> Optional[MLJob]: ...

    @abstractmethod
    def find_all_ordered(self) -> list[MLJob]: ...


class MLResultRepository(BaseRepository[MLResult]):
    @abstractmethod
    def find_latest_by_mode(self, mode: str) -> Optional[MLResult]: ...
