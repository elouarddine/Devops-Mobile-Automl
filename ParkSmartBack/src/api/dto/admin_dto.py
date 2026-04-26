from pydantic import BaseModel, Field, HttpUrl, model_validator


class DatasetImportRequest(BaseModel):
    dataset_name: str = Field(min_length=2, max_length=150)
    source_type: str = Field(pattern="^(url|file)$")
    source_url: HttpUrl | None = None
    file_path: str | None = None

    @model_validator(mode="after")
    def validate_source(self):
        if self.source_type == "url" and not self.source_url:
            raise ValueError("source_url est requis quand source_type=url")
        if self.source_type == "file" and not self.file_path:
            raise ValueError("file_path est requis quand source_type=file")
        return self
