import re

from app.models.summarization_model import SummarizationModel
from app.schemas import GeneratedReport, SentimentSection
from app.services.sentiment_analysis import SentimentAnalysisService


class ReportService:
    def __init__(self) -> None:
        self.summarizer = SummarizationModel()
        self.sentiment_service = SentimentAnalysisService()

    def generate_report(self, transcribed_text: str, meeting_title: str | None, language: str | None) -> GeneratedReport:
        summary = self.summarizer.summarize(transcribed_text)
        key_points = self._extract_key_points(summary)
        action_items = self._extract_action_items(transcribed_text)
        entities = self._extract_entities(transcribed_text)
        sentiment = self.sentiment_service.analyze(transcribed_text)

        return GeneratedReport(
            meeting_title=meeting_title,
            language=language,
            summary=summary,
            key_points=key_points,
            action_items=action_items,
            sentiment_analysis=SentimentSection(
                label=sentiment["label"],
                score=sentiment["score"],
            ),
            entities=entities,
        )

    def _extract_key_points(self, summary: str) -> list[str]:
        candidates = [line.strip(" -\t") for line in summary.split("\n") if line.strip()]
        if len(candidates) == 1:
            candidates = [c.strip() for c in re.split(r"[.;]", candidates[0]) if c.strip()]
        return candidates[:8] or [summary]

    def _extract_action_items(self, text: str) -> list[str]:
        action_patterns = (
            r"\b(action item|todo|to do|next step|suivi|a faire|il faut|must|should)\b.*",
            r"\b(assign|responsable|owner|deadline|date limite)\b.*",
        )
        lines = [line.strip() for line in text.splitlines() if line.strip()]
        actions: list[str] = []
        for line in lines:
            lowered = line.lower()
            if any(re.search(pattern, lowered) for pattern in action_patterns):
                actions.append(line)
        if not actions:
            # fallback: first imperative-like phrases
            for chunk in re.split(r"[.!?]\s+", text):
                cleaned = chunk.strip()
                if re.match(r"^(please|merci de|veuillez|let's|nous devons|on doit)\b", cleaned.lower()):
                    actions.append(cleaned)
                if len(actions) >= 5:
                    break
        return actions[:10]

    def _extract_entities(self, text: str) -> dict[str, list[str]]:
        # Lightweight extraction fallback to avoid hard runtime dependency on large NLP models.
        emails = sorted(set(re.findall(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}", text)))
        dates = sorted(set(re.findall(r"\b(?:\d{1,2}[/-]){1,2}\d{2,4}\b", text)))
        times = sorted(set(re.findall(r"\b\d{1,2}:\d{2}\b", text)))
        names = sorted(set(re.findall(r"\b[A-Z][a-z]{2,}\s+[A-Z][a-z]{2,}\b", text)))

        return {
            "people": names[:20],
            "dates": dates[:20],
            "times": times[:20],
            "emails": emails[:20],
        }
