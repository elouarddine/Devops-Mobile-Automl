class AppException(Exception):
    def __init__(self, message: str, code: str, status_code: int, details: dict | None = None):
        super().__init__(message)
        self.message = message
        self.code = code
        self.status_code = status_code
        self.details = details or {}


class BadRequestError(AppException):
    def __init__(self, message: str, code: str = "BAD_REQUEST", details: dict | None = None):
        super().__init__(message, code, 400, details)


class UnauthorizedError(AppException):
    def __init__(self, message: str = "Non authentifié", code: str = "UNAUTHORIZED", details: dict | None = None):
        super().__init__(message, code, 401, details)


class ForbiddenError(AppException):
    def __init__(self, message: str = "Accès interdit", code: str = "FORBIDDEN", details: dict | None = None):
        super().__init__(message, code, 403, details)


class NotFoundError(AppException):
    def __init__(self, message: str, code: str = "NOT_FOUND", details: dict | None = None):
        super().__init__(message, code, 404, details)


class ConflictError(AppException):
    def __init__(self, message: str, code: str = "CONFLICT", details: dict | None = None):
        super().__init__(message, code, 409, details)
