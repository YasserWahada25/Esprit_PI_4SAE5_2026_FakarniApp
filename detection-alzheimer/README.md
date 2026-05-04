# Detection Alzheimer - ML Service

## Setup

### 1. Download ML Model
The trained model is too large for Git. Download it separately:
- Model file: `alzheimer_final_v3/variables/variables.data-00000-of-00001` (127MB)
- Place it in: `detection-alzheimer/detection-alzheimer/alzheimer_final_v3/variables/`

### 2. Create Virtual Environment
```bash
cd detection-alzheimer/detection-alzheimer
python -m venv venv
```

### 3. Activate Virtual Environment

**Windows:**
```bash
venv\Scripts\activate
```

**Linux/Mac:**
```bash
source venv/bin/activate
```

### 4. Install Dependencies
```bash
pip install -r requirements.txt
```

### 5. Run the Service
```bash
python app.py
```

## Notes

- The `venv` folder is NOT included in git (it's in `.gitignore`)
- The ML model files are NOT included in git (too large - 127MB)
- Each developer must create their own virtual environment
- All dependencies are listed in `requirements.txt`
- Store the ML model in cloud storage (AWS S3, Google Drive, etc.) and download it during deployment
