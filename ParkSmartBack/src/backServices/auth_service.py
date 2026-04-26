from src.api.errors.exceptions import ConflictError, NotFoundError, UnauthorizedError
from src.backServices.logging_service import get_logger
from src.security.jwt_handler import create_access_token
from src.security.password import hash_password, verify_password
from src.storage.db_models.enums import UserRole
from src.storage.db_models.tables import User

logger = get_logger("auth")


class AuthService:
    def __init__(self, user_repository):
        self.user_repository = user_repository

    def register(self, full_name: str, email: str, password: str, role: str) -> User:
        normalized_email = email.lower().strip()
        existing = self.user_repository.find_by_email(normalized_email)
        if existing:
            logger.warning(f"Registration refused for existing email={normalized_email}")
            raise ConflictError("Email déjà utilisé", code="EMAIL_ALREADY_EXISTS", details={"field": "email"})

        user = User(
            full_name=full_name.strip(),
            email=normalized_email,
            hashed_password=hash_password(password),
            role=UserRole(role.upper()),
        )
        saved = self.user_repository.save(user)
        logger.info(f"User registered email={normalized_email} role={saved.role.value}")
        return saved

    def login(self, email: str, password: str) -> dict:
        normalized_email = email.lower().strip()
        user = self.user_repository.find_by_email(normalized_email)
        if not user:
            logger.warning(f"Login failed: account not found email={normalized_email}")
            raise NotFoundError("Compte introuvable", code="ACCOUNT_NOT_FOUND")

        if not verify_password(password, user.hashed_password):
            logger.warning(f"Login failed: wrong credentials email={normalized_email}")
            raise UnauthorizedError("Email ou mot de passe incorrect", code="WRONG_CREDENTIALS")

        token = create_access_token({"sub": user.email, "role": user.role.value, "user_id": user.id})
        logger.info(f"Login success email={normalized_email} role={user.role.value}")
        return {
            "access_token": token,
            "token_type": "Bearer",
            "role": user.role.value,
            "user_id": str(user.id),
            "username": user.full_name,
            "full_name": user.full_name,
        }
