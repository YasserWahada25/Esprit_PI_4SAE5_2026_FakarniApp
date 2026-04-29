from fastapi import HTTPException, UploadFile

from app.models.whisper_model import WhisperModel


class TranscriptionService:
    def __init__(self) -> None:
        self.model = WhisperModel()

    async def transcribe_audio(self, file: UploadFile, language: str | None = None) -> dict:
        content = await file.read()
        if not content:
            raise HTTPException(status_code=400, detail="Empty audio file.")

        try:
            result = self.model.transcribe(content=content, filename=file.filename or "audio.wav", language=language)
        except RuntimeError as exc:
            raise HTTPException(status_code=500, detail=str(exc)) from exc

        return result
