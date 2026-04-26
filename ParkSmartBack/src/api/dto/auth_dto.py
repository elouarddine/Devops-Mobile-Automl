from pydantic import AliasChoices, BaseModel, ConfigDict, EmailStr, Field, field_validator


class RegisterRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    full_name: str = Field(
        min_length=2,
        max_length=150,
        validation_alias=AliasChoices("full_name", "fullName"),
        serialization_alias="full_name",
    )
    email: EmailStr
    password: str = Field(min_length=6, max_length=128)
    role: str = Field(default="USER")

    @field_validator("role")
    @classmethod
    def validate_role(cls, value: str) -> str:
        normalized = value.strip().upper()
        if normalized in {"UTILISATEUR", "USER"}:
            return "USER"
        if normalized in {"ADMIN", "ADMINISTRATEUR"}:
            return "ADMIN"
        raise ValueError("role invalide (USER ou ADMIN)")


class LoginRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=1, max_length=128)
