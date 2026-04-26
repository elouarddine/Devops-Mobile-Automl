from dataclasses import dataclass

from src.backServices.admin_ml_service import AdminMLService
from src.backServices.auth_service import AuthService
from src.backServices.history_service import HistoryService
from src.backServices.saved_search_service import SavedSearchService
from src.backServices.search_service import SearchService


@dataclass
class ParkSmartOrchestrator:
    auth_service: AuthService
    search_service: SearchService
    history_service: HistoryService
    saved_search_service: SavedSearchService
    admin_ml_service: AdminMLService
