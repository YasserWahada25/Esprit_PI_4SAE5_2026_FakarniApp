from app.models.sentiment_model import SentimentModel


class SentimentAnalysisService:
    def __init__(self) -> None:
        self.model = SentimentModel()

    def analyze(self, text: str) -> dict:
        return self.model.analyze(text)
