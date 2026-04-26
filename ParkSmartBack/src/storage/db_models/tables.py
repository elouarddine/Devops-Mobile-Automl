from datetime import datetime

from sqlalchemy import Boolean, DateTime, Enum, Float, ForeignKey, Integer, JSON, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from src.storage.database import Base
from .base import IdMixin
from .enums import JobStatus, JobType, UserRole


class User(IdMixin, Base):
    __tablename__ = "users"

    full_name: Mapped[str] = mapped_column(String(150), nullable=False)
    email: Mapped[str] = mapped_column(String(255), unique=True, nullable=False, index=True)
    hashed_password: Mapped[str] = mapped_column(String(255), nullable=False)
    role: Mapped[UserRole] = mapped_column(Enum(UserRole), nullable=False, default=UserRole.USER)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)


class Parking(IdMixin, Base):
    __tablename__ = "parkings"

    external_id: Mapped[str | None] = mapped_column(String(120), unique=True, nullable=True)
    name: Mapped[str] = mapped_column(String(150), nullable=False)
    address: Mapped[str | None] = mapped_column(String(255), nullable=True)
    latitude: Mapped[float] = mapped_column(Float, nullable=False)
    longitude: Mapped[float] = mapped_column(Float, nullable=False)
    capacity: Mapped[int | None] = mapped_column(Integer, nullable=True)
    current_free_places: Mapped[int | None] = mapped_column(Integer, nullable=True)
    price_per_hour: Mapped[float | None] = mapped_column(Float, nullable=True)
    source_name: Mapped[str | None] = mapped_column(String(120), nullable=True)
    metadata_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)


class SearchHistory(IdMixin, Base):
    __tablename__ = "search_history"

    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), nullable=False, index=True)
    destination_text: Mapped[str] = mapped_column(String(255), nullable=False)
    destination_lat: Mapped[float | None] = mapped_column(Float, nullable=True)
    destination_lon: Mapped[float | None] = mapped_column(Float, nullable=True)
    arrival_option: Mapped[str] = mapped_column(String(50), nullable=False)
    radius_m: Mapped[int] = mapped_column(Integer, nullable=False, default=2000)
    recommended_parking_name: Mapped[str | None] = mapped_column(String(150), nullable=True)
    recommended_parking_id: Mapped[int | None] = mapped_column(ForeignKey("parkings.id"), nullable=True)
    results_json: Mapped[list | None] = mapped_column(JSON, nullable=True)
    searched_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)


class SavedSearch(IdMixin, Base):
    __tablename__ = "saved_searches"

    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), nullable=False, index=True)
    title: Mapped[str] = mapped_column(String(150), nullable=False)
    destination_text: Mapped[str] = mapped_column(String(255), nullable=False)
    arrival_option: Mapped[str] = mapped_column(String(50), nullable=False)
    recommended_parking_name: Mapped[str | None] = mapped_column(String(150), nullable=True)
    payload_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)


class SavedParking(IdMixin, Base):
    __tablename__ = "saved_parkings"
    __table_args__ = (UniqueConstraint("user_id", "parking_external_id", name="uq_saved_parking_user_external"),)

    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), nullable=False, index=True)
    parking_external_id: Mapped[str] = mapped_column(String(120), nullable=False, index=True)
    parking_name: Mapped[str] = mapped_column(String(150), nullable=False)
    address: Mapped[str | None] = mapped_column(String(255), nullable=True)
    latitude: Mapped[float | None] = mapped_column(Float, nullable=True)
    longitude: Mapped[float | None] = mapped_column(Float, nullable=True)
    distance_m: Mapped[int | None] = mapped_column(Integer, nullable=True)
    capacity: Mapped[int | None] = mapped_column(Integer, nullable=True)
    current_free_places: Mapped[int | None] = mapped_column(Integer, nullable=True)
    predicted_free_places: Mapped[int | None] = mapped_column(Integer, nullable=True)
    price_per_hour: Mapped[float | None] = mapped_column(Float, nullable=True)
    arrival_option: Mapped[str | None] = mapped_column(String(50), nullable=True)
    metadata_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    saved_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)


class DatasetImport(IdMixin, Base):
    __tablename__ = "datasets"

    dataset_name: Mapped[str] = mapped_column(String(150), unique=True, nullable=False)
    source_type: Mapped[str] = mapped_column(String(50), nullable=False)
    source_url: Mapped[str | None] = mapped_column(Text, nullable=True)
    file_path: Mapped[str | None] = mapped_column(Text, nullable=True)
    imported_by_user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    imported_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)


class MLJob(IdMixin, Base):
    __tablename__ = "ml_jobs"

    job_type: Mapped[JobType] = mapped_column(Enum(JobType), nullable=False)
    status: Mapped[JobStatus] = mapped_column(Enum(JobStatus), nullable=False, default=JobStatus.PENDING)
    dataset_id: Mapped[int | None] = mapped_column(ForeignKey("datasets.id"), nullable=True)
    launched_by_user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), nullable=False)
    started_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)
    finished_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    duration_sec: Mapped[int | None] = mapped_column(Integer, nullable=True)
    message: Mapped[str | None] = mapped_column(Text, nullable=True)


class MLResult(IdMixin, Base):
    __tablename__ = "ml_results"

    mode: Mapped[str] = mapped_column(String(50), nullable=False)
    job_id: Mapped[int | None] = mapped_column(ForeignKey("ml_jobs.id"), nullable=True)
    metrics_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    charts_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    summary: Mapped[str | None] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)
