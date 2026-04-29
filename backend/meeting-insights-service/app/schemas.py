from datetime import datetime
from pydantic import BaseModel, Field


class ReportRequest(BaseModel):
    meeting_title: str | None = Field(default=None, description="Optional meeting title.")
    language: str | None = Field(default=None, description="Language hint such as fr/en/ar.")
    transcribed_text: str = Field(..., description="Meeting transcription text.")
    transcription_id: str | None = Field(
        default=None,
        description="Optional existing transcription identifier.",
    )


class TranscriptionResponse(BaseModel):
    id: str
    requested_by: str
    filename: str
    language: str | None = None
    transcription: str
    created_at: datetime


class SentimentSection(BaseModel):
    label: str
    score: float


class GeneratedReport(BaseModel):
    id: str | None = None
    transcription_id: str | None = None
    meeting_title: str | None = None
    language: str | None = None
    generated_by: str | None = None
    summary: str
    key_points: list[str]
    action_items: list[str]
    sentiment_analysis: SentimentSection
    entities: dict[str, list[str]]
    created_at: datetime | None = None
