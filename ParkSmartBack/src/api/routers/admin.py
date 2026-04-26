from fastapi import APIRouter, Depends, status

from src.api.dependencies.auth import require_admin
from src.api.dto.admin_dto import DatasetImportRequest
from src.backServices.admin_ml_service import AdminMLService
from src.backServices.automl_bridge_service import AutoMLBridgeService
from src.backServices.response_service import success_response
from src.storage.database import get_db
from src.storage.repositories.sqlalchemy_repositories import SQLAlchemyDatasetRepository, SQLAlchemyMLJobRepository, SQLAlchemyMLResultRepository

router = APIRouter(prefix="/admin", tags=["admin"])


def _service(db):
    return AdminMLService(
        dataset_repository=SQLAlchemyDatasetRepository(db),
        job_repository=SQLAlchemyMLJobRepository(db),
        result_repository=SQLAlchemyMLResultRepository(db),
        automl_bridge=AutoMLBridgeService(),
    )


@router.get("/dashboard")
def dashboard(_=Depends(require_admin), db=Depends(get_db)):
    return success_response(_service(db).get_dashboard())


@router.post("/datasets/import", status_code=status.HTTP_201_CREATED)
def import_dataset(payload: DatasetImportRequest, current_admin=Depends(require_admin), db=Depends(get_db)):
    dataset = _service(db).import_dataset(
        dataset_name=payload.dataset_name,
        source_type=payload.source_type,
        source_url=str(payload.source_url) if payload.source_url else None,
        file_path=payload.file_path,
        admin_user_id=current_admin.id,
    )
    return success_response({"dataset_id": dataset.id, "dataset_name": dataset.dataset_name})


@router.post("/train")
def train(current_admin=Depends(require_admin), db=Depends(get_db)):
    job = _service(db).launch_train(current_admin.id)
    return success_response({"job_id": f"job_{job.id}"})


@router.post("/evaluate")
def evaluate(current_admin=Depends(require_admin), db=Depends(get_db)):
    job = _service(db).launch_evaluate(current_admin.id)
    return success_response({"job_id": f"job_{job.id}"})


@router.get("/results")
def results(mode: str = "eval", _=Depends(require_admin), db=Depends(get_db)):
    return success_response(_service(db).get_results(mode))


@router.get("/jobs")
def jobs(_=Depends(require_admin), db=Depends(get_db)):
    return success_response({"jobs": _service(db).get_jobs()})
