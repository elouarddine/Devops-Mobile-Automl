import os

from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from src.api.errors.handlers import register_exception_handlers
from src.api.routers import admin, auth, auth_v1, health, history, search
from src.backServices.logging_service import configure_logging, get_logger, request_logging_middleware
from src.storage.database import SessionLocal
from src.storage.db_models.tables import Parking

load_dotenv()
configure_logging()
logger = get_logger("Démarrage")


def seed_demo_parkings() -> None:
    db = SessionLocal()
    try:
        demo_parkings = [
            Parking(external_id="lemans_republique", name="Parking République", address="Place de la République, Le Mans", latitude=48.0055, longitude=0.1995, capacity=250, current_free_places=11, price_per_hour=2.20, source_name="seed"),
            Parking(external_id="lemans_jacobins", name="Parking Jacobins", address="13 Place des Jacobins, Le Mans", latitude=48.0072, longitude=0.1977, capacity=300, current_free_places=24, price_per_hour=2.50, source_name="seed"),
            Parking(external_id="lemans_novaxis", name="Parking Novaxis", address="Boulevard Demorieux, Le Mans", latitude=48.0048, longitude=0.2023, capacity=150, current_free_places=8, price_per_hour=1.80, source_name="seed"),
            Parking(external_id="toulouse_capitole", name="Parking Capitole", address="Place du Capitole, Toulouse", latitude=43.6045, longitude=1.4440, capacity=220, current_free_places=19, price_per_hour=2.60, source_name="seed"),
            Parking(external_id="lyon_bellecour", name="Parking Bellecour", address="Place Bellecour, Lyon", latitude=45.7578, longitude=4.8320, capacity=280, current_free_places=16, price_per_hour=3.10, source_name="seed"),
            Parking(external_id="nantes_graslin", name="Parking Graslin", address="Place Graslin, Nantes", latitude=47.2136, longitude=-1.5621, capacity=210, current_free_places=12, price_per_hour=2.40, source_name="seed"),
            Parking(external_id="strasbourg_kleber", name="Parking Kléber", address="Place Kléber, Strasbourg", latitude=48.5837, longitude=7.7451, capacity=260, current_free_places=18, price_per_hour=2.80, source_name="seed"),
        ]

        existing_ids = {item.external_id for item in db.query(Parking).all() if item.external_id}
        to_insert = [item for item in demo_parkings if item.external_id not in existing_ids]
        if not to_insert:
            logger.info("Demo des parkings déjà présents, aucun ajout nécessaire")
            return

        db.add_all(to_insert)
        db.commit()
        logger.info("Seeded %s demo parkings", len(to_insert))
    finally:
        db.close()


def create_app() -> FastAPI:
    app = FastAPI(title=os.getenv("APP_NAME", "ParkSmart"), version="1.0.0")

    origins = [origin.strip() for origin in os.getenv("APP_CORS_ORIGINS", "*").split(",") if origin.strip()]
    app.add_middleware(
        CORSMiddleware,
        allow_origins=origins or ["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.middleware("http")(request_logging_middleware)

    register_exception_handlers(app)

    app.include_router(health.router)
    app.include_router(auth.router)
    app.include_router(search.router, prefix="/v1")
    app.include_router(history.router, prefix="/v1")
    app.include_router(admin.router, prefix="/v1")
   # app.include_router(auth_v1.router, prefix="/parkSmart")

    @app.on_event("startup")
    def on_startup() -> None:
        logger.info("Starting ParkSmart API")
        seed_demo_parkings()

    return app


app = create_app()
