from fastapi import APIRouter

from src.backServices.response_service import success_response

router = APIRouter(tags=["health"])


@router.get("/health")
def healthcheck():
    return success_response({"api_status": "OK"})
