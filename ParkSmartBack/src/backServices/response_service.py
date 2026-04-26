def success_response(data, status: str = "success") -> dict:
    return {"status": status, "data": data, "error": None}
