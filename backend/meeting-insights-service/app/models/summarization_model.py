import os
import re


class SummarizationModel:
    def __init__(self) -> None:
        self.model_name = os.getenv("SUMMARIZATION_MODEL", "facebook/bart-large-cnn")
        self._pipeline = None

    def summarize(self, text: str) -> str:
        cleaned = " ".join(text.split())
        if not cleaned:
            return ""

        # Try Hugging Face summarization pipeline first.
        pipe = self._get_pipeline()
        if pipe is not None:
            try:
                max_len = max(80, min(220, len(cleaned.split()) // 2))
                result = pipe(cleaned, max_length=max_len, min_length=40, do_sample=False)
                if result and isinstance(result, list):
                    summary = result[0].get("summary_text", "").strip()
                    if summary:
                        return summary
            except Exception:
                pass

        # Fallback extractive summary: first key sentences.
        sentences = re.split(r"(?<=[.!?])\s+", cleaned)
        return " ".join(sentences[:4]).strip()

    def _get_pipeline(self):
        if self._pipeline is not None:
            return self._pipeline
        try:
            from transformers import pipeline
        except Exception:
            self._pipeline = None
            return None
        try:
            self._pipeline = pipeline("summarization", model=self.model_name)
        except Exception:
            self._pipeline = None
        return self._pipeline
