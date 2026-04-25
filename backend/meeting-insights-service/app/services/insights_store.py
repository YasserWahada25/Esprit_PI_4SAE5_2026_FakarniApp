from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime, timezone
from threading import Lock

from app.schemas import GeneratedReport, TranscriptionResponse


@dataclass(slots=True)
class InsightsStore:
    _lock: Lock = field(default_factory=Lock)
    _transcriptions: list[TranscriptionResponse] = field(default_factory=list)
    _reports: list[GeneratedReport] = field(default_factory=list)

    def add_transcription(self, item: TranscriptionResponse) -> None:
        with self._lock:
            self._transcriptions.append(item)

    def add_report(self, item: GeneratedReport) -> None:
        with self._lock:
            self._reports.append(item)

    def list_transcriptions(self) -> list[TranscriptionResponse]:
        with self._lock:
            return list(reversed(self._transcriptions))

    def list_reports(self) -> list[GeneratedReport]:
        with self._lock:
            return list(reversed(self._reports))

    @staticmethod
    def now_utc() -> datetime:
        return datetime.now(timezone.utc)

