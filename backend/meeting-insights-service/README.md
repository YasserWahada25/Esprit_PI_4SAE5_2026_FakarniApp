# Meeting Insights Service

Microservice AI pour FakarniApp qui traite les enregistrements audio de réunion :
- transcription voix -> texte
- résumé automatique + points clés
- analyse de sentiment
- extraction d'entités
- génération d'un rapport structuré

## Structure ajoutée

```text
meeting-insights-service/
├── app/
│   ├── main.py
│   ├── schemas.py
│   ├── services/
│   │   ├── transcription_service.py
│   │   ├── report_service.py
│   │   └── sentiment_analysis.py
│   └── models/
│       ├── whisper_model.py
│       ├── summarization_model.py
│       └── sentiment_model.py
├── requirements.txt
├── Dockerfile
└── README.md
```

## Description des fichiers

- `app/main.py`
  - Point d'entrée FastAPI
  - Endpoints `/api/meet/recording` et `/api/meet/report`
  - Vérification JWT pour sécuriser les endpoints

- `app/schemas.py`
  - Contrats de requête/réponse (`ReportRequest`, `TranscriptionResponse`, `GeneratedReport`)

- `app/services/transcription_service.py`
  - Orchestration de la transcription audio via `WhisperModel`

- `app/services/report_service.py`
  - Génération du rapport complet : résumé, points clés, actions, sentiment, entités

- `app/services/sentiment_analysis.py`
  - Wrapper dédié à l'analyse de sentiment

- `app/models/whisper_model.py`
  - Transcription via OpenAI Whisper API (`OPENAI_API_KEY`) ou fallback local `openai-whisper`

- `app/models/summarization_model.py`
  - Résumé via pipeline Hugging Face (`BART` par défaut), fallback extractif

- `app/models/sentiment_model.py`
  - Sentiment via VADER, fallback heuristique lexical

- `requirements.txt`
  - Dépendances Python pour l'API et le NLP

- `Dockerfile`
  - Image de déploiement FastAPI sur port `8086`

## Sécurité JWT

Tous les endpoints métier sont protégés par Bearer token.

- Header attendu : `Authorization: Bearer <jwt>`
- Variables:
  - `JWT_SECRET` (par défaut même secret que les autres services Fakarni)
  - `JWT_ALGORITHM` (défaut `HS256`)

## Endpoints

### 1) POST `/api/meet/recording`

Transcrit un fichier audio.

- `multipart/form-data`
  - `file`: fichier audio (obligatoire)
  - `language`: hint optionnel (`fr`, `en`, `ar`, ...)

Réponse exemple:

```json
{
  "requested_by": "user-id",
  "filename": "meeting.wav",
  "language": "fr",
  "transcription": "Texte transcrit..."
}
```

### 2) POST `/api/meet/report`

Génère un rapport structuré depuis une transcription.

Body exemple:

```json
{
  "meeting_title": "Réunion hebdo équipe soins",
  "language": "fr",
  "transcribed_text": "..."
}
```

Réponse exemple:

```json
{
  "meeting_title": "Réunion hebdo équipe soins",
  "language": "fr",
  "generated_by": "user-id",
  "summary": "...",
  "key_points": ["...", "..."],
  "action_items": ["..."],
  "sentiment_analysis": {
    "label": "positive",
    "score": 0.62
  },
  "entities": {
    "people": ["..."],
    "dates": ["..."],
    "times": ["..."],
    "emails": ["..."]
  }
}
```

## Lancer en local

```bash
cd meeting-insights-service
python -m venv .venv
. .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8086
```

## Variables utiles

- `OPENAI_API_KEY`: active la transcription Whisper via OpenAI API
- `WHISPER_MODEL`: modèle OpenAI (défaut `whisper-1`)
- `LOCAL_WHISPER_MODEL`: modèle local whisper (défaut `base`)
- `SUMMARIZATION_MODEL`: modèle HF pour le résumé (`facebook/bart-large-cnn`)
- `JWT_SECRET`: secret JWT partagé entre services
- `JWT_ALGORITHM`: algo JWT (`HS256`)

## Notes d'intégration backend FakarniApp

- Exposer ce service derrière la Gateway (ex: route `/api/meet/**` -> port 8086)
- Conserver le même `JWT_SECRET` que `User-Service`/`Session-Service`
- Optionnel: persister les rapports générés dans un store dédié (MongoDB/PostgreSQL)
- Optionnel: ajouter export PDF/Word à partir de `GeneratedReport`
