from fastapi import Depends
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from src.api.errors.exceptions import ForbiddenError, UnauthorizedError
from src.security.jwt_handler import TokenDecodeError, decode_access_token
from src.storage.database import get_db
from src.storage.db_models.enums import UserRole
from src.storage.repositories.sqlalchemy_repositories import SQLAlchemyUserRepository

bearer_scheme = HTTPBearer(auto_error=False)


def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(bearer_scheme),
    db=Depends(get_db),
):
    if not credentials:
        raise UnauthorizedError("Token absent")

    try:
        payload = decode_access_token(credentials.credentials)
    except TokenDecodeError as exc:
        raise UnauthorizedError(str(exc)) from exc

    email = payload.get("sub")
    if not email:
        raise UnauthorizedError("Token invalide")

    user = SQLAlchemyUserRepository(db).find_by_email(email)
    if not user:
        raise UnauthorizedError("Utilisateur introuvable pour ce token")

    return user


def require_admin(current_user=Depends(get_current_user)):
    if current_user.role != UserRole.ADMIN:
        raise ForbiddenError("Réservé aux administrateurs")
    return current_user
