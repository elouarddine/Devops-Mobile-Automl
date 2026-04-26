import logging
import os
import sys
import time
import uuid
from contextvars import ContextVar

from fastapi import Request

_request_id_ctx_var: ContextVar[str] = ContextVar("request_id", default="-")


class _ColorFormatter(logging.Formatter):
    COLORS = {
        "DEBUG": "\033[36m",
        "INFO": "\033[32m",
        "WARNING": "\033[33m",
        "ERROR": "\033[31m",
        "CRITICAL": "\033[1;31m",
    }
    RESET = "\033[0m"

    def format(self, record: logging.LogRecord) -> str:
        request_id = getattr(record, "request_id", _request_id_ctx_var.get())
        base = (
            f"[{record.name}] [{record.levelname}] "
            f"[req:{request_id}] {record.getMessage()}"
        )
        if record.exc_info:
            base += "\n" + self.formatException(record.exc_info)
        if sys.stdout.isatty():
            color = self.COLORS.get(record.levelname, "")
            return f"{color}{base}{self.RESET}"
        return base


def configure_logging() -> None:
    level = os.getenv("LOG_LEVEL", "INFO").upper()
    root_logger = logging.getLogger()
    root_logger.handlers.clear()
    root_logger.setLevel(level)

    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(_ColorFormatter())
    root_logger.addHandler(handler)

    for logger_name in ("uvicorn", "uvicorn.error", "uvicorn.access", "sqlalchemy.engine"):
        logger = get_logger(logger_name)
        logger.handlers.clear()
        logger.propagate = True
        logger.setLevel(level if logger_name != "sqlalchemy.engine" else os.getenv("SQL_LOG_LEVEL", "WARNING").upper())


def get_logger(name: str) -> logging.Logger:
    return logging.getLogger(name)


async def request_logging_middleware(request: Request, call_next):
    request_id = request.headers.get("X-Request-ID", str(uuid.uuid4())[:8])
    token = _request_id_ctx_var.set(request_id)
    logger = get_logger("api")
    start = time.perf_counter()
    logger.info(f"➡️  {request.method} {request.url.path}")
    try:
        response = await call_next(request)
    except Exception:
        logger.exception(f"💥 {request.method} {request.url.path} failed")
        _request_id_ctx_var.reset(token)
        raise

    duration_ms = round((time.perf_counter() - start) * 1000, 2)
    response.headers["X-Request-ID"] = request_id
    logger.info(f"⬅️  {request.method} {request.url.path} -> {response.status_code} ({duration_ms} ms)")
    _request_id_ctx_var.reset(token)
    return response
