import os
import tempfile


class WhisperModel:
    def transcribe(self, content: bytes, filename: str, language: str | None = None) -> dict:
        """
        Strategy:
        1) Use OpenAI Whisper API if OPENAI_API_KEY is provided.
        2) Else try local open-source whisper package.
        3) Else raise a clear runtime error.
        """
        api_key = os.getenv("OPENAI_API_KEY")
        if api_key:
            return self._transcribe_with_openai(content=content, filename=filename, language=language)
        return self._transcribe_with_local_whisper(content=content, filename=filename, language=language)

    def _transcribe_with_openai(self, content: bytes, filename: str, language: str | None = None) -> dict:
        try:
            from openai import OpenAI
        except Exception as exc:
            raise RuntimeError("openai package is missing for OpenAI Whisper API.") from exc

        client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))
        with tempfile.NamedTemporaryFile(delete=True, suffix=self._guess_suffix(filename)) as tmp:
            tmp.write(content)
            tmp.flush()
            with open(tmp.name, "rb") as audio_file:
                response = client.audio.transcriptions.create(
                    model=os.getenv("WHISPER_MODEL", "whisper-1"),
                    file=audio_file,
                    language=language,
                )
        text = (getattr(response, "text", None) or "").strip()
        if not text:
            raise RuntimeError("Transcription returned an empty text.")
        return {"transcription": text, "language": language}

    def _transcribe_with_local_whisper(self, content: bytes, filename: str, language: str | None = None) -> dict:
        try:
            import whisper
        except Exception as exc:
            raise RuntimeError(
                "No transcription backend available. Set OPENAI_API_KEY or install open-source whisper."
            ) from exc

        model_name = os.getenv("LOCAL_WHISPER_MODEL", "base")
        model = whisper.load_model(model_name)
        with tempfile.NamedTemporaryFile(delete=True, suffix=self._guess_suffix(filename)) as tmp:
            tmp.write(content)
            tmp.flush()
            result = model.transcribe(tmp.name, language=language)
        text = (result.get("text") or "").strip()
        if not text:
            raise RuntimeError("Local whisper returned an empty transcription.")
        detected_language = result.get("language") or language
        return {"transcription": text, "language": detected_language}

    def _guess_suffix(self, filename: str) -> str:
        if "." in filename:
            return "." + filename.rsplit(".", 1)[-1].lower()
        return ".wav"
