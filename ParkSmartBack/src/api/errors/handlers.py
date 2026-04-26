import os

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from src.backServices.logging_service import get_logger
from .exceptions import AppException

logger = get_logger("errors")


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(AppException)
    async def app_exception_handler(_: Request, exc: AppException):
        logger.warning(f"{exc.code}: {exc.message}")
        return JSONResponse(
            status_code=exc.status_code,
            content={
                "status": "error",
                "data": None,
                "error": {
                    "code": exc.code,
                    "message": exc.message,
                    "details": exc.details,
                },
            },
        )

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(_: Request, exc: RequestValidationError):
        first_error = exc.errors()[0] if exc.errors() else {}
        field = ".".join([str(x) for x in first_error.get("loc", ["body"]) if x != "body"])
        logger.warning(f"VALIDATION_ERROR on field={field}: {first_error.get('msg', 'Données invalides')}")
        return JSONResponse(
            status_code=400,
            content={
                "status": "error",
                "data": None,
                "error": {
                    "code": "VALIDATION_ERROR",
                    "message": first_error.get("msg", "Données invalides"),
                    "details": {"field": field},
                },
            },
        )

    @app.exception_handler(Exception)
    async def generic_exception_handler(_: Request, exc: Exception):
        logger.exception("Unhandled exception")
        details = {}
        if os.getenv("APP_DEBUG", "false").lower() in {"1", "true", "yes"}:
            details = {"debug": str(exc)}
        return JSONResponse(
            status_code=500,
            content={
                "status": "error",
                "data": None,
                "error": {
                    "code": "INTERNAL_SERVER_ERROR",
                    "message": "Erreur serveur interne",
                    "details": details,
                },
            },
        )
