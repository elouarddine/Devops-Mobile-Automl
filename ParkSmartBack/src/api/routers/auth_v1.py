from fastapi import APIRouter, Depends, status

from src.api.dependencies.auth import get_current_user
from src.api.dto.auth_dto import LoginRequest, RegisterRequest
from src.backServices.auth_service import AuthService
from src.backServices.response_service import success_response
from src.storage.database import get_db
from src.storage.repositories.sqlalchemy_repositories import SQLAlchemyUserRepository

router = APIRouter(tags=["auth-v1"])


def _service(db):
    return AuthService(SQLAlchemyUserRepository(db))


@router.post("/auth/register", status_code=status.HTTP_201_CREATED)
def register_v1(payload: RegisterRequest, db=Depends(get_db)):
    user = _service(db).register(payload.full_name, payload.email, payload.password, payload.role)
    return success_response({"user_id": str(user.id), "role": user.role.value})


@router.post("/auth/login")
def login_v1(payload: LoginRequest, db=Depends(get_db)):
    return success_response(_service(db).login(payload.email, payload.password))


@router.get("/auth/me")
def me_v1(current_user=Depends(get_current_user)):
    return success_response(
        {
            "user_id": str(current_user.id),
            "full_name": current_user.full_name,
            "email": current_user.email,
            "role": current_user.role.value,
        }
    )
