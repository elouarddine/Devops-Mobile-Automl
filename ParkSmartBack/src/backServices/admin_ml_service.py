from datetime import datetime

from src.api.errors.exceptions import BadRequestError, ConflictError, NotFoundError
from src.storage.db_models.enums import JobStatus, JobType
from src.storage.db_models.tables import DatasetImport, MLJob, MLResult


class AdminMLService:
    def __init__(self, dataset_repository, job_repository, result_repository, automl_bridge):
        self.dataset_repository = dataset_repository
        self.job_repository = job_repository
        self.result_repository = result_repository
        self.automl_bridge = automl_bridge

    def import_dataset(self, dataset_name: str, source_type: str, source_url: str | None, file_path: str | None, admin_user_id: int) -> DatasetImport:
        active = self.dataset_repository.find_active()
        if active:
            active.is_active = False
            self.dataset_repository.update(active)

        dataset = DatasetImport(
            dataset_name=dataset_name,
            source_type=source_type,
            source_url=source_url,
            file_path=file_path,
            imported_by_user_id=admin_user_id,
            is_active=True,
        )
        return self.dataset_repository.save(dataset)

    def launch_train(self, admin_user_id: int) -> MLJob:
        running = self.job_repository.find_running_job()
        if running:
            raise ConflictError("Un job est déjà en cours", code="JOB_ALREADY_RUNNING")

        dataset = self.dataset_repository.find_active()
        if not dataset:
            raise BadRequestError("Aucun dataset actif", code="DATASET_REQUIRED")

        job = MLJob(job_type=JobType.TRAIN, status=JobStatus.RUNNING, dataset_id=dataset.id, launched_by_user_id=admin_user_id)
        job = self.job_repository.save(job)
        result = self.automl_bridge.train(dataset.file_path or dataset.source_url)
        self._finish_job(job, result, mode="train")
        return job

    def launch_evaluate(self, admin_user_id: int) -> MLJob:
        running = self.job_repository.find_running_job()
        if running:
            raise ConflictError("Un job est déjà en cours", code="JOB_ALREADY_RUNNING")

        last_train = self.result_repository.find_latest_by_mode("train")
        if not last_train:
            raise BadRequestError("Veuillez lancer un entraînement avant l’évaluation.", code="TRAINING_REQUIRED")

        dataset = self.dataset_repository.find_active()
        job = MLJob(job_type=JobType.EVALUATE, status=JobStatus.RUNNING, dataset_id=dataset.id if dataset else None, launched_by_user_id=admin_user_id)
        job = self.job_repository.save(job)
        result = self.automl_bridge.evaluate(dataset.file_path or dataset.source_url if dataset else None)
        self._finish_job(job, result, mode="eval")
        return job

    def _finish_job(self, job: MLJob, result: dict, mode: str) -> None:
        now = datetime.utcnow()
        job.finished_at = now
        job.duration_sec = int((now - job.started_at).total_seconds())
        job.status = JobStatus.DONE
        job.message = result.get("summary")
        self.job_repository.update(job)

        ml_result = MLResult(
            mode=mode,
            job_id=job.id,
            metrics_json=result.get("metrics"),
            charts_json=result.get("charts"),
            summary=result.get("summary"),
        )
        self.result_repository.save(ml_result)

    def get_dashboard(self) -> dict:
        dataset = self.dataset_repository.find_active()
        latest_train = self.result_repository.find_latest_by_mode("train")
        return {
            "api_status": "OK",
            "model": {"name": "ParkSmart_model_v1", "task_type": "regression"},
            "dataset": {
                "name": dataset.dataset_name if dataset else None,
                "last_update": dataset.imported_at.date().isoformat() if dataset else None,
            },
            "last_training_summary": latest_train.summary if latest_train else None,
        }

    def get_results(self, mode: str) -> dict:
        result = self.result_repository.find_latest_by_mode(mode)
        if not result:
            raise NotFoundError("Aucun résultat disponible", code="RESULT_NOT_FOUND")
        return {
            "mode": result.mode,
            "metrics": result.metrics_json or {},
            "charts": result.charts_json or {},
            "summary": result.summary,
        }

    def get_jobs(self) -> list[dict]:
        jobs = self.job_repository.find_all_ordered()
        return [
            {
                "job_id": f"job_{job.id}",
                "type": job.job_type.value,
                "status": job.status.value,
                "started_at": job.started_at.isoformat(),
                "duration_sec": job.duration_sec,
                "message": job.message,
            }
            for job in jobs
        ]
