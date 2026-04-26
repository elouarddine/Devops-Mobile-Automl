import os

from src.security.password import hash_password
from src.storage.database import SessionLocal
from src.storage.db_models.enums import UserRole
from src.storage.db_models.tables import User


def seed_admin() -> None:
    db = SessionLocal()
    try:
        email = os.getenv("ADMIN_SEED_EMAIL", "admin@parksmart.com")
        existing = db.query(User).filter(User.email == email).first()
        if existing:
            print("Admin seed already present.")
            return

        admin = User(
            full_name=os.getenv("ADMIN_SEED_FULL_NAME", "Admin ParkSmart"),
            email=email,
            hashed_password=hash_password(os.getenv("ADMIN_SEED_PASSWORD", "Admin1234")),
            role=UserRole.ADMIN,
        )
        db.add(admin)
        db.commit()
        print("Admin seed created.")
    finally:
        db.close()


if __name__ == "__main__":
    seed_admin()
