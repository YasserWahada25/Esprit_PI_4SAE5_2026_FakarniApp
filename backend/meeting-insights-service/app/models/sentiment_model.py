class SentimentModel:
    def __init__(self) -> None:
        self._analyzer = None
        try:
            from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

            self._analyzer = SentimentIntensityAnalyzer()
        except Exception:
            self._analyzer = None

    def analyze(self, text: str) -> dict:
        if not text.strip():
            return {"label": "neutral", "score": 0.0}

        if self._analyzer:
            score = self._analyzer.polarity_scores(text)["compound"]
            if score >= 0.2:
                label = "positive"
            elif score <= -0.2:
                label = "negative"
            else:
                label = "neutral"
            return {"label": label, "score": round(float(score), 4)}

        # Simple fallback lexical heuristic
        positive_words = ("good", "great", "excellent", "merci", "bien", "super", "positive")
        negative_words = ("bad", "problem", "issue", "risk", "fail", "negative", "mauvais")
        lowered = text.lower()
        pos = sum(lowered.count(w) for w in positive_words)
        neg = sum(lowered.count(w) for w in negative_words)
        if pos > neg:
            return {"label": "positive", "score": 0.3}
        if neg > pos:
            return {"label": "negative", "score": -0.3}
        return {"label": "neutral", "score": 0.0}
