from pydantic import BaseModel, Field


class ReportRequest(BaseModel):
    meeting_title: str | None = Field(default=None, description="Optional meeting title.")
    language: str | None = Field(default=None, description="Language hint such as fr/en/ar.")
    transcribed_text: str = Field(..., description="Meeting transcription text.")


class TranscriptionResponse(BaseModel):
    requested_by: str
    filename: str
    language: str | None = None
    transcription: str


class SentimentSection(BaseModel):
    label: str
    score: float


class GeneratedReport(BaseModel):
    meeting_title: str | None = None
    language: str | None = None
    generated_by: str | None = None
    summary: str
    key_points: list[str]
    action_items: list[str]
    sentiment_analysis: SentimentSection
    entities: dict[str, list[str]]
