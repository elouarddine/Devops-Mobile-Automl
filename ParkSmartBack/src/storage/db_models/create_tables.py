from src.storage.database import Base, engine
from src.storage.db_models import tables  # noqa: F401

print("Création automatique des tables PostgreSQL...")
Base.metadata.create_all(bind=engine)
print("Tables créées avec succès.")
