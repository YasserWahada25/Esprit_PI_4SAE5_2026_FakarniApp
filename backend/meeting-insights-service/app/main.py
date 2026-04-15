from fastapi import Depends, FastAPI, File, HTTPException, UploadFile
from fastapi.security import OAuth2PasswordBearer
import jwt
from jwt import InvalidTokenError

from app.services.report_service import ReportService
from app.services.transcription_service import TranscriptionService
from app.schemas import GeneratedReport, ReportRequest, TranscriptionResponse


app = FastAPI(
    title="Meeting Insights Service",
    version="1.0.0",
    description="Transcription, meeting insights extraction, sentiment analysis and report generation.",
)

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")


def verify_token(token: str = Depends(oauth2_scheme)) -> dict:
    import os

    secret = os.getenv(
        "JWT_SECRET",
        "ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=",
    )
    algorithm = os.getenv("JWT_ALGORITHM", "HS256")
    try:
        payload = jwt.decode(token, secret, algorithms=[algorithm])
    except InvalidTokenError as exc:
        raise HTTPException(status_code=401, detail="Invalid JWT token.") from exc

    user_id = payload.get("sub")
    role = (payload.get("role") or "").upper()
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload: missing subject.")
    return {"user_id": user_id, "role": role, "claims": payload}


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/api/meet/recording", response_model=TranscriptionResponse)
async def transcribe_audio(
    file: UploadFile = File(...),
    language: str | None = None,
    auth: dict = Depends(verify_token),
) -> TranscriptionResponse:
    if not file.filename:
        raise HTTPException(status_code=400, detail="Missing audio filename.")

    transcription_service = TranscriptionService()
    result = await transcription_service.transcribe_audio(file=file, language=language)
    return TranscriptionResponse(
        requested_by=auth["user_id"],
        filename=file.filename,
        language=result.get("language"),
        transcription=result["transcription"],
    )


@app.post("/api/meet/report", response_model=GeneratedReport)
async def generate_report(
    request: ReportRequest,
    auth: dict = Depends(verify_token),
) -> GeneratedReport:
    if not request.transcribed_text.strip():
        raise HTTPException(status_code=400, detail="transcribed_text cannot be empty.")

    report_service = ReportService()
    report = report_service.generate_report(
        transcribed_text=request.transcribed_text,
        meeting_title=request.meeting_title,
        language=request.language,
    )
    report.generated_by = auth["user_id"]
    return report
